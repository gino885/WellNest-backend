package com.wellnest.comic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ChatTTSService {

    private static final String API_URL = "https://api.replicate.com/v1/predictions";
    private static final String API_TOKEN = System.getenv("REPLICATE_API_TOKEN");
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    public String generateSpeech(String text) throws IOException {
        String version = "fdb4f547d19c9591d7e0223c88b14886c110129c0e206ddbb97fe7a344162868";

        Map<String, Object> input = new HashMap<>();
        input.put("text", text);
        input.put("voice", 7869);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", version);
        requestBody.put("input", input);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        log.info("Requesting speech generation for text: {}", text);

        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String predictionId = jsonNode.path("id").asText();
                log.info("Speech generation initiated, prediction ID: {}", predictionId);
                return predictionId;
            } else {
                throw new IOException("Request failed with code: " + response.code() + " and response: " + response.body().string());
            }
        }
    }

    public void saveAudio(String text, String fileName) throws IOException, InterruptedException {
        log.info("Starting audio save for text: {}", text);
        String predictionId = generateSpeech(text);

        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            Request request = new Request.Builder()
                    .url(API_URL + "/" + predictionId)
                    .addHeader("Authorization", "Bearer " + API_TOKEN)
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Polling request failed with code: " + response.code());
                }

                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String status = jsonNode.path("status").asText();
                log.info("Polling status: {}", status);
                if ("succeeded".equals(status)) {
                    JsonNode audioFilesNode = jsonNode.path("output").path("audio_files");
                    String audioUrl = audioFilesNode.get(0).path("filename").asText();
                    log.info("Audio URL retrieved: {}", audioUrl);
                    downloadAudio(audioUrl, fileName);
                    break;
                } else if ("failed".equals(status) || "canceled".equals(status)) {
                    throw new IOException("Prediction failed.");
                }
                log.info("Response body: {}", responseBody);
            }

            TimeUnit.SECONDS.sleep(5);
        }
    }
    public void generateNarration(String combinedText) throws IOException, InterruptedException {
        String[] texts = combinedText.split("\n");

        for (int i = 0; i < texts.length; i++) {
            String textSegment = texts[i];
            int segmentIndex = i;
            String fileName = "src/voice/comic_" + segmentIndex + ".mp3";
            log.info("Generating narration for segment {}: {}", segmentIndex, textSegment);
            try {
                saveAudio(textSegment, fileName);
            } catch (IOException | InterruptedException e) {
                log.error("Failed to generate narration for segment {}", segmentIndex, e);
                throw e;  // 抛出异常以便调用者捕获
            }
        }
    }



    private void downloadAudio(String audioUrl, String fileName) throws IOException {
        Request request = new Request.Builder().url(audioUrl).build();
        log.info("Starting download from URL: {}", audioUrl);
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download audio with code: " + response.code());
            }

            File outputFile = new File(fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] audioBytes = response.body().bytes();
                fos.write(audioBytes);
                log.info("Successfully wrote {} bytes to file: {}", audioBytes.length, outputFile.getAbsolutePath());
            }
        }
    }
}
