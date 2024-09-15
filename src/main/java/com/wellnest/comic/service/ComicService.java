package com.wellnest.comic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.comic.model.ComicJSON;
import com.wellnest.comic.model.ComicRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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

    private S3Client s3Client;
    private String bucketName = "wellnestbucket";

    public ComicService() {
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTHEAST_2)
                .build();
    }

    public List<String> generateComic(String description) throws JsonProcessingException, IOException {
        String version = "39c85f153f00e4e9328cb3035b94559a8ec66170eb4c0618c07b16528bf20ac2";
        int numLines = description.split("\n").length;
        System.out.println(description.split("\n"));
        int numIds = Math.min(3, numLines);
        System.out.printf("numlines：%s, numIds：%s", numLines, numIds);

        ComicJSON.InputParams inputParams = ComicJSON.InputParams.builder()
                .comicDescription(description)
                .characterDescription("a man, wearing black suit")
                .sdModel("Unstable")
                .numSteps(30)
                .styleName("Disney Charactor")
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

        return pollPredictionStatus(predictionId);
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

    public List<String> pollPredictionStatus(String predictionId) throws IOException {
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
                    System.out.println(jsonNode.path("output"));
                    jsonNode.path("output").path("individual_images").forEach(urlNode -> outputUrls.add(urlNode.asText()));
                    System.out.println(outputUrls);

                    List<Integer> customOrder = Arrays.asList(3, 2, 1, 0);

                    int index = 0;
                    for (int i : customOrder) {
                        if (i < outputUrls.size()) {
                            savedFilePaths.add(saveImageFromUrl(outputUrls.get(i), index++));
                        }
                    }

                    for (int i = customOrder.size(); i < outputUrls.size(); i++) {
                        savedFilePaths.add(saveImageFromUrl(outputUrls.get(i), index++));
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
    private String saveImageFromUrl(String imageUrl, int index) throws IOException {
        BufferedImage image = downloadImageFromUrl(imageUrl);
        if (image != null) {
            return saveImageToFile(image, index);
        }
        return null;
    }

    private BufferedImage downloadImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String saveImageToFile(BufferedImage image, int index) throws IOException {
        LocalDate currentDate = LocalDate.now();
        String dateString = currentDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "webp", os)) {
            throw new IOException("Failed to convert image to bytes");
        }
        byte[] imageBytes = os.toByteArray();
        String objectKey = "comic_images/"+ dateString +"/comic_" + index + ".webp";

         PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("image/webp")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

        String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + objectKey;

        return s3Url;
    }

}
