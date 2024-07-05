package com.wellnest.comic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.comic.model.ComicJSON;
import com.wellnest.comic.model.ComicRequest;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class ComicService {
    @Autowired
    private OkHttpClient okHttpClient;

    private static final String API_URL = "https://api.replicate.com/v1/predictions";
    private static final String API_TOKEN = System.getenv("REPLICATE_API_TOKEN");

    public String generateComic(ComicRequest comicRequest) throws JsonProcessingException {
        String version = "39c85f153f00e4e9328cb3035b94559a8ec66170eb4c0618c07b16528bf20ac2";


        ComicJSON.InputParams.InputParamsBuilder inputBuilder = ComicJSON.InputParams.builder()
                .comicDescription(comicRequest.getComicDescription())
                .characterDescription(comicRequest.getCharacterDescription())
                .sdModel("Unstable")
                .numSteps(25)
                .styleName("Japanese Anime")
                .comicStyle("Classic Comic Style")
                .imageWidth(768)
                .imageHeight(768)
                .outputFormat("webp")
                .negativePrompt("bad anatomy, bad hands, missing fingers, extra fingers, three hands, three legs, bad arms, missing legs, missing arms, poorly drawn face, bad face, fused face, cloned face, three crus, fused feet, fused thigh, extra crus, ugly fingers, horn, cartoon, cg, 3d, unreal, animate, amputation, disconnected limbs")
                .style_strengthRatio(40);

        if (comicRequest.getRefImageUrl() != null && !comicRequest.getRefImageUrl().isEmpty()) {
            inputBuilder.refImage(comicRequest.getRefImageUrl());
        }

        ComicJSON input = ComicJSON.builder()
                .version(version)
                .input(inputBuilder.build())
                .build();

        RequestBody body = RequestBody.create(input.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .addHeader("Content-Type", "application/json")
                .build();

        final String[] predictionId = {null};
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("Response: " + responseBody);
                    predictionId[0] = parsePredictionId(responseBody); // 解析预测 ID
                } else {
                    System.out.println("Request failed with code: " + response.code());
                }
            }
        });
        return predictionId[0];
    }

    private String parsePredictionId(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(responseBody).get("id").asText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

        public String pollPredictionStatus(String predictionId) throws InterruptedException {
            while (true) {
                Request request = new Request.Builder()
                        .url(API_URL + predictionId)
                        .get()
                        .addHeader("Authorization", "Bearer " + API_TOKEN)
                        .build();

                final String[] status = {null};
                final String[] output = {null};

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            System.out.println("Response: " + responseBody);
                            status[0] = parseStatus(responseBody);
                            output[0] = parseOutput(responseBody); // 获取输出结果
                        } else {
                            System.out.println("Request failed with code: " + response.code());
                        }
                    }
                });

                if (status[0] != null && (status[0].equals("succeeded") || status[0].equals("failed") || status[0].equals("canceled"))) {
                    return output[0];
                }

                Thread.sleep(5000); // 等待 5 秒钟，然后再次检查状态
            }
        }

        private String parseStatus(String responseBody) {
            // 解析 JSON 响应以提取状态
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(responseBody).get("status").asText();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private String parseOutput(String responseBody) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(responseBody).get("output").asText();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
}



