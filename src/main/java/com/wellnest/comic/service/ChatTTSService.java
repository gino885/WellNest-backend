package com.wellnest.comic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.comic.model.AudioFile;
import jakarta.persistence.criteria.CriteriaBuilder;
import jdk.swing.interop.SwingInterOpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatTTSService {

    private static final String API_URL = "https://api.replicate.com/v1/predictions";
    private static final String API_TOKEN = System.getenv("REPLICATE_API_TOKEN");
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    public String generateSpeech(String text, Integer voice) throws IOException {
        String version = "fdb4f547d19c9591d7e0223c88b14886c110129c0e206ddbb97fe7a344162868";

        Map<String, Object> input = new HashMap<>();
        input.put("text", text);
        input.put("voice", voice);
        input.put("temperature", 0.3);
        input.put("top_k", 20);
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

    public String saveAudio(String text, String fileName) throws IOException, InterruptedException {
        log.info("Starting audio save for text: {}", text);
        String predictionId = "";
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");

        String formattedDate = dateFormat.format(date);
        if (fileName.startsWith("src/voice/" + formattedDate+ "/n")){
            predictionId = generateSpeech(text, 1259);
        } else if (fileName.startsWith("src/voice/" + formattedDate+ "/d")){
            predictionId = generateSpeech(text, 4099);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String savedFilePath;
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
                    savedFilePath = downloadAudio(audioUrl, fileName);
                    break;
                } else if ("failed".equals(status) || "canceled".equals(status)) {
                    throw new IOException("Prediction failed.");
                }
                log.info("Response body: {}", responseBody);
            }

            TimeUnit.SECONDS.sleep(5);
        }
        return savedFilePath;
    }

    public List<String> processNarrationAndDialogue(String apiOutput) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");

        String formattedDate = dateFormat.format(date);

        String directoryPath = "src/voice/" + formattedDate;
        Pattern pattern = Pattern.compile("\\[(Narration|Dialogue)_(\\d+)\\](.*)");
        List<String> audioFilePaths = new ArrayList<>();
        HashMap<Integer, Integer> sceneCounter = new HashMap<>();

        for (String line : apiOutput.split("\n")) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String type = matcher.group(1);
                int sceneNumber = Integer.parseInt(matcher.group(2));
                String content = matcher.group(3).trim();

                sceneCounter.putIfAbsent(sceneNumber, 0);

                sceneCounter.put(sceneNumber, sceneCounter.get(sceneNumber) + 1);
                int currentOrder = sceneCounter.get(sceneNumber);

                String filename;
                if (type.equals("Narration")) {
                    filename = directoryPath + "/n_" + sceneNumber + "_" + currentOrder + ".mp3";
                } else {
                    filename = directoryPath + "/d_" + sceneNumber + "_" + currentOrder + ".mp3";
                }

                try {
                    System.out.println("fileName" + filename);
                    audioFilePaths.add(saveAudio(content, filename));
                } catch (IOException | InterruptedException e) {
                    log.error("Failed to generate narration for segment {}", filename, e);
                }
            }
        }
        return audioFilePaths;

}
    private String downloadAudio(String audioUrl, String fileName) throws IOException {
        Request request = new Request.Builder().url(audioUrl).build();

        File outputFile = new File(fileName);

        File parentDir = outputFile.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }
        log.info("Starting download from URL: {}", audioUrl);
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download audio with code: " + response.code());
            }
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] audioBytes = response.body().bytes();
                fos.write(audioBytes);
                log.info("Successfully wrote {} bytes to file: {}", audioBytes.length, outputFile.getAbsolutePath());
            }
            return outputFile.getAbsolutePath();
        }
    }
}