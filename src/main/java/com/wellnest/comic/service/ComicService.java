package com.wellnest.comic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.service.ScaleService;
import com.wellnest.comic.dao.ComicRepo;
import com.wellnest.comic.model.Comic;
import com.wellnest.comic.model.ComicJSON;
import com.wellnest.comic.model.ComicRequest;
import com.wellnest.user.dao.UserDao;
import com.wellnest.user.enmus.Gender;
import com.wellnest.user.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.hibernate.annotations.CurrentTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ComicService {
    @Autowired
    private OkHttpClient okHttpClient;
    private static final Logger logger = LoggerFactory.getLogger(ComicService.class);
    private static final String API_URL = "https://api.replicate.com/v1/predictions";
    private static final String API_TOKEN = System.getenv("REPLICATE_API_TOKEN");

    @Autowired
    private ChatDao chatDao;
    @Autowired
    private ComicRepo comicRepo;

    @Autowired
    private UserDao userDao;
    private S3Client s3Client;
    private String bucketName = "wellnestbucket";

    public ComicService() {
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTHEAST_2)
                .build();
    }

    public List<String> generateComic(String description, Integer chatId, String userId, String[] captions) throws JsonProcessingException, IOException {
        String version = "39c85f153f00e4e9328cb3035b94559a8ec66170eb4c0618c07b16528bf20ac2";
        int numLines = description.split("\n").length;
        System.out.println(description.split("\n"));
        int numIds = Math.min(3, numLines);
        System.out.printf("numlines：%s, numIds：%s", numLines, numIds);
        User user = userDao.getUserById(Integer.parseInt(userId));
        String characterDescription = String.format("a %s-years-old man, wearing a white T-shirt", user.getAge());
        if (user.getGender().equals(Gender.FEMALE) ){
            characterDescription = String.format("a %s-years-old woman, wearing a white T-shirt", user.getAge());
        }

        ComicJSON.InputParams inputParams = ComicJSON.InputParams.builder()
                .comicDescription(description)
                .characterDescription(characterDescription)
                .sdModel("Unstable")
                .numSteps(50)
                .styleName("Japanese Anime")
                .comicStyle("Classic Comic Style")
                .imageWidth(512)
                .imageHeight(512)
                .outputFormat("webp")
                .negativePrompt("bad anatomy, bad hands, missing fingers, extra fingers, three hands, three legs, bad arms, missing legs, missing arms, poorly drawn face, bad face, fused face, cloned face, three crus, fused feet, fused thigh, extra crus, ugly fingers, horn, cartoon, cg, 3d, unreal, animate, amputation, disconnected limbs")
                .style_strengthRatio(20)
                .numIds(4)
                .guidanceScale(5)
                .sa32Setting(0.6)
                .sa64Setting(0.6)
                .outputQuality(80)
                .build();

        ComicJSON input = ComicJSON.builder()
                .version(version)
                .input(inputParams)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(input);
        logger.info("JSON Payload: {}", jsonPayload);

        okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();
        String predictionId;
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                logger.info("Initial response: {}", responseBody);
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                predictionId = jsonNode.path("id").asText();
                logger.info(predictionId);
            } else {
                logger.error("Initial request failed with code: {} and response body: {}", response.code(), response.body().string());
                return null;
            }
        }

        return pollPredictionStatus(predictionId, chatId, userId, captions);
    }

    private String parsePredictionId(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(responseBody).get("id").asText();
        } catch (IOException e) {
            logger.error("Failed to parse prediction ID", e);
            return null;
        }
    }

    public List<String> pollPredictionStatus(String predictionId, Integer chatId, String userId, String[] captions) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> outputUrls = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();
        while (true) {

            Request request = new Request.Builder()
                    .url("https://api.replicate.com/v1/predictions/" + predictionId)
                    .addHeader("Authorization", "Bearer " + API_TOKEN)
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Polling request failed with code: " + response.code());
                }

                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String status = jsonNode.path("status").asText();
                logger.info(status);
                if ("succeeded".equals(status)) {
                    Date date = new Date();

                    System.out.println(jsonNode.path("output"));
                    jsonNode.path("output").path("individual_images").forEach(urlNode -> outputUrls.add(urlNode.asText()));
                    System.out.println(outputUrls);

                    List<Integer> customOrder = Arrays.asList(3, 2, 1, 0);

                    int index = 0;
                    for (int i : customOrder) {
                        if (i < outputUrls.size()) {
                            Comic comic = new Comic();
                            comic.setUserId(Integer.parseInt(userId));
                            comic.setChatId(chatId);
                            comic.setDate(date);
                            comic.setType("comic");
                            comic.setAttribute(captions[i]);
                            String url = saveImageToFile(outputUrls.get(i), index++, chatId);
                            comic.setUrl(url);
                            comic.setPage(i);
                            savedFilePaths.add(url);
                            comicRepo.save(comic);
                        }
                    }

                    for (int i = customOrder.size(); i < outputUrls.size(); i++) {
                        Comic comic = new Comic();
                        comic.setUserId(Integer.parseInt(userId));
                        comic.setChatId(chatId);
                        comic.setDate(date);
                        comic.setType("comic");
                        comic.setAttribute(captions[i]);
                        String url = saveImageToFile(outputUrls.get(i), index++, chatId);
                        comic.setUrl(url);
                        comic.setPage(i);
                        savedFilePaths.add(url);
                        comicRepo.save(comic);
                    }

                    break;
                } else if ("failed".equals(status) || "canceled".equals(status)) {
                    throw new IOException("Prediction failed.");
                }
            }

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Polling interrupted", e);
            }
        }
        return savedFilePaths;
    }

    private String saveImageToFile(String imageUrl, int index, Integer chatId) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        String s3Url = "";
        try (InputStream inputStream = connection.getInputStream()) {
            LocalDate currentDate = LocalDate.now();
            String dateString = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String objectKey = "comic_images/" + dateString + "/" + chatId + "/comic_" + index + ".webp";

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType("image/webp")
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(inputStream, connection.getContentLengthLong()));

            s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + objectKey;
        } catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return s3Url;
    }

}
