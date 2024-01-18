package com.wellnest.service.impl;

import com.wellnest.service.PlayHtService;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class playHtServiceImpl implements PlayHtService {
    @Override
    public String getAudio(String text) {

            HttpClient client = HttpClient.newHttpClient();
            try {
                String json = String.format("{\"content\":[\"%s\"],\"voice\":\"zh-TW-YunJheNeural\",\"title\":\"wellnest\",\"globalSpeed\":\"120%%\"}", text);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.play.ht/api/v1/convert"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("AUTHORIZATION", "8e33295fa8164e60b876860043324c99")
                        .header("X-USER-ID", "IotFmLA8tUgFmsH0RiSqUZBocso2")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                while(true){
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    JSONObject jsonResponse = new JSONObject(response.body());
                    System.out.println(jsonResponse);
                    if("CREATED".equals(jsonResponse.getString("status"))){
                        String transcriptionId = jsonResponse.getString("transcriptionId");

                        return getAudioUrl(transcriptionId);
                    }
                    Thread.sleep(500);
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
    }

    @Override
    public String getAudioUrl(String transcribeId) {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("https://api.play.ht/api/v1/articleStatus?transcriptionId=%s",transcribeId);
        System.out.println(url);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("accept", "application/json")
                    .header("AUTHORIZATION", "8e33295fa8164e60b876860043324c99")
                    .header("X-USER-ID", "IotFmLA8tUgFmsH0RiSqUZBocso2")
                    .GET()
                    .build();

            while (true) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject jsonResponse = new JSONObject(response.body());
                if (jsonResponse.getBoolean("converted")){
                    return jsonResponse.getString("audioUrl");
                }
                Thread.sleep(500);
            }

        }
       catch (Exception e){
            e.printStackTrace();
       }
        return null;
    }
}


