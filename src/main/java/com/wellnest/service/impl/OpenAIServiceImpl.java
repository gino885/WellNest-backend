package com.wellnest.service.impl;

import com.wellnest.service.OpenAIService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class OpenAIServiceImpl implements OpenAIService {

    private final String apiKey;
    private String assistant_id = "asst_aq76m7dKoN2Hze2dhgNiAuds";

    public OpenAIServiceImpl(@Value("${API_KEY}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String createThread() {
        String url = "https://api.openai.com/v1/threads";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v1")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String addMessageToThread(String threadId, String message) {
        String url = String.format("https://api.openai.com/v1/threads/%s/messages", threadId);
        String requestBody = String.format("{\"role\": \"user\", \"content\": \"%s\"}", message);

        return "";
    }

    @Override
    public String runThread(String threadId) {
        String url = "https://api.openai.com/v1/threads/" + threadId + "/runs";
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = String.format("{\"assistant_id\": \"%s\"}", assistant_id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v1")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());

            while (true) {
                String status = getRunStatus(threadId, jsonResponse.getString("id"));
                if ("completed".equals(status)) {
                    return jsonResponse.getString("id");
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "interrupted";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }


    @Override
    public JSONArray addMessage(String threadId, String message) {
        String url = String.format("https://api.openai.com/v1/threads/%s/messages", threadId);
        HttpClient client = HttpClient.newHttpClient();

        String jsonBody = String.format("{\"role\": \"user\",\"content\": \"%s\"}", message);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v1")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getJSONArray("content");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String getRespond(String threadId) {
        runThread(threadId);
        String url = "https://api.openai.com/v1/threads/" + threadId + "/messages";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("OpenAI-Beta", "assistants=v1")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());
            JSONArray dataArray = jsonObject.getJSONArray("data");

// 初始化用於尋找最新消息的變量
            JSONObject latestMessage = null;
            long latestTime = 0;

// 遍歷 dataArray 尋找最新的消息
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject message = dataArray.getJSONObject(i);
                long createdAt = message.getLong("created_at");

                if (createdAt > latestTime) {
                    latestMessage = message;
                    latestTime = createdAt;
                }
            }

// 從最新的消息中獲取 value
            if (latestMessage != null) {
                JSONArray contentArray = latestMessage.getJSONArray("content");
                if (contentArray.length() > 0) {
                    JSONObject firstContent = contentArray.getJSONObject(0);
                    JSONObject textObject = firstContent.getJSONObject("text");
                    String value = textObject.getString("value");
                    return value;
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public String getRunStatus(String threadId, String runId) {

        String url = "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("OpenAI-Beta", "assistants=v1")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("status");
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 或者返回一个错误信息
        }
    }

    public byte[] textToSpeech(String threadId, String inputText) {
        addMessage(threadId, inputText);
        HttpClient client = HttpClient.newHttpClient();
        String respond = getRespond(threadId);
        try {
        JSONObject requestBody = new JSONObject()
                .put("model", "tts-1")
                .put("voice", "alloy")
                .put("input",  getRespond(threadId));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/audio/speech"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();


            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body(); // 返回音频数据的字节数组
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 或处理异常
        }
    }
}




