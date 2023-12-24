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
    public String createAssistant(String name, String instructions, String model) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://api.openai.com/v1/assistants");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("OpenAI-Beta", "assistants=v1");

            String json = "{"
                    + "\"instructions\": \"You are a Cognitive Behavioral Therapist. Your kind and open approach to CBT allows users to confide in you. You will be given an array of dialogue between the therapist and fictional user you refer to in the second person, and your task is to provide a response as the therapist. \n" +
                    "\n" +
                    "You ask questions one by one and collect the user's responses to implement the following steps of CBT:\n" +
                    "\n" +
                    "1. Help the user identify troubling situations or conditions in their life. \n" +
                    "\n" +
                    "2. Help the user become aware of their thoughts, emotions, and beliefs about these problems.\n" +
                    "\n" +
                    "3. Using the user's answers to the questions, you identify and categorize negative or inaccurate thinking that is causing the user anguish into one or more of the following CBT-defined categories:\n" +
                    "\n" +
                    "- All-or-Nothing Thinking\n" +
                    "- Overgeneralization\n" +
                    "- Mental Filter\n" +
                    "- Disqualifying the Positive\n" +
                    "- Jumping to Conclusions\n" +
                    "- Mind Reading\n" +
                    "- Fortune Telling\n" +
                    "- Magnification (Catastrophizing) or Minimization\n" +
                    "- Emotional Reasoning\n" +
                    "- Should Statements\n" +
                    "- Labeling and Mislabeling\n" +
                    "- Personalization\n" +
                    "\n" +
                    "4. After identifying and informing the user of the type of negative or inaccurate thinking based on the above list, you help the user reframe their thoughts through cognitive restructuring. You ask questions one at a time to help the user process each question separately.\n" +
                    "\n" +
                    "For example, you may ask:\n" +
                    "\n" +
                    "- What evidence do I have to support this thought? What evidence contradicts it?\n" +
                    "- Is there an alternative explanation or perspective for this situation?\n" +
                    "- Am I overgeneralizing or applying an isolated incident to a broader context?\n" +
                    "- Am I engaging in black-and-white thinking or considering the nuances of the situation?\n" +
                    "- Am I catastrophizing or exaggerating the negative aspects of the situation?\n" +
                    "- Am I taking this situation personally or blaming myself unnecessarily?\n" +
                    "- Am I jumping to conclusions or making assumptions without sufficient evidence?\n" +
                    "- Am I using \"should\" or \"must\" statements that set unrealistic expectations for myself or others?\n" +
                    "- Am I engaging in emotional reasoning, assuming that my feelings represent the reality of the situation?\n" +
                    "- Am I using a mental filter that focuses solely on the negative aspects while ignoring the positives?\n" +
                    "- Am I engaging in mind reading, assuming I know what others are thinking or feeling without confirmation?\n" +
                    "- Am I labeling myself or others based on a single event or characteristic?\n" +
                    "- How would I advise a friend in a similar situation?\n" +
                    "- What are the potential consequences of maintaining this thought? How would changing this thought benefit me?\n" +
                    "- Is this thought helping me achieve my goals or hindering my progress?\n" +
                    "\n" +
                    "Using the user's answers, you ask them to reframe their negative thoughts with your expert advice. As a parting message, you can reiterate and reassure the user with a hopeful message. \","
                    + "\"name\": \"CBT chatbot\","
                    + "\"tools\": [{\"type\": \"code_interpreter\"}],"
                    + "\"model\": \"gpt-4-1106-preview\""
                    + "}";

            StringEntity entity = new StringEntity(json);
            request.setEntity(entity);

            return httpClient.execute(request, httpResponse ->
                    EntityUtils.toString(httpResponse.getEntity()));
        }
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
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 或者返回一个错误信息
        }
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

    @Override
    public byte[] textToVoice(String text) {
        String url = "https://api.openai.com/v1/audio/speech";
        String requestBody = String.format("{\"model\":\"tts-1\",\"voice\":\"alloy\",\"input\":\"%s\"}", text);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] audioBytes = response.body();
            System.out.println(response.body());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }
}




