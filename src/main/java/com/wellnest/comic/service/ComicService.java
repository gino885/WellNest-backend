package com.wellnest.comic.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
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

import java.io.IOException;
import java.util.ArrayList;
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

    private static final String PIC_URL = "https://replicate.delivery/pbxt/KrYifi1qCInaAdHZqMlSY0BN5O3vegkLfU8fRCBKu3iqbKXL/1.jpeg";

    public List generateComic(ComicRequest comicRequest) throws JsonProcessingException, IOException{
        String version = "39c85f153f00e4e9328cb3035b94559a8ec66170eb4c0618c07b16528bf20ac2";
        int numLines = comicRequest.getCharacterDescription().split("\n").length;
        System.out.println(comicRequest.getCharacterDescription().split("\n"));
        int numIds = Math.min(3, numLines);
        System.out.printf("numlines：%s, numIds：%s",numLines, numIds);

        ComicJSON.InputParams inputParams = ComicJSON.InputParams.builder()
                .comicDescription(comicRequest.getComicDescription())
                .characterDescription(comicRequest.getCharacterDescription())
                .sdModel("Unstable")
                .numSteps(25)
                .styleName(comicRequest.getStyleName())
                .comicStyle("Classic Comic Style")
                .imageWidth(512)
                .imageHeight(512)
                .outputFormat("webp")
                .negativePrompt("bad anatomy, bad hands, missing fingers, extra fingers, three hands, three legs, bad arms, missing legs, missing arms, poorly drawn face, bad face, fused face, cloned face, three crus, fused feet, fused thigh, extra crus, ugly fingers, horn, cartoon, cg, 3d, unreal, animate, amputation, disconnected limbs")
                .style_strengthRatio(20)
                .numIds(3)
                .guidanceScale(5)
                .sa32Setting(0.5)
                .sa64Setting(0.5)
                .outputQuality(80)
                .build();

        ComicJSON input = ComicJSON.builder()
                .version(version)
                .input(inputParams)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(input);
        logger.info("JSON Payload: {}", jsonPayload);

        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();
        System.out.println(API_TOKEN);
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
        return outputUrls;
    }



    private String parseStatus(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(responseBody).get("status").asText();
        } catch (IOException e) {
            logger.error("Failed to parse status", e);
            return null;
        }
    }

    private String parseOutput(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode outputNode = objectMapper.readTree(responseBody).get("output");
            return outputNode != null ? outputNode.asText() : null;
        } catch (IOException e) {
            logger.error("Failed to parse output", e);
            return null;
        }
    }
}
