package com.wellnest.chatbot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.chatbot.dao.MissionDao;
import com.wellnest.chatbot.model.Emotion;
import com.wellnest.chatbot.model.Mission;
import com.wellnest.chatbot.service.MissionService;
import jdk.swing.interop.SwingInterOpUtils;
import jep.Interpreter;
import jep.SharedInterpreter;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class MissionServiceImpl implements MissionService {
    @Autowired
    private MissionDao missionDao;

    public List<Mission> getMission(String content) {
        try {
            OkHttpClient client = new OkHttpClient();
            System.out.println(content);

            String json = "{\"text\":\"" + content + "\"}";
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("https://24a3-34-105-124-8.ngrok-free.app/predict")
                    .post(body)
                    .build();
            String jsonRespond = "";
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    jsonRespond= response.body().string();
                    System.out.println(jsonRespond);
                } else {
                    System.out.println("Request failed with status: " + response.code());
                }
            }
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonRespond);
            JsonNode predictions = rootNode.get("predictions");

            Map<String, Double> emotionScores = new HashMap<>();

            for (JsonNode node : predictions) {
                String label = node.get("label").asText();
                double score = node.get("score").asDouble();
                emotionScores.put(label, score);
            }
            Map<Mission, Double> missionScores = new HashMap<>();
            List<Mission> final_missions = new ArrayList<>();
            List<Mission> missions;
            for (int difficulty = 0; difficulty < 2; difficulty++){
                missionScores.clear();
                if(difficulty == 0){
                    missions = missionDao.findByDifficulty(difficulty);
                } else {
                    missions = missionDao.findByDifficulty(difficulty);
                    missions.addAll(missionDao.findByDifficulty(difficulty + 1));
                }
                for (Mission mission : missions) {
                    double score = 0;
                    for (Emotion emotion : mission.getEmotions()) {
                        score += emotionScores.getOrDefault(emotion.getName(), 0.0);
                    }
                    missionScores.put(mission, score);
                }
                final_missions.addAll(getTopMissions(missionScores, difficulty));
            }
            System.out.println(final_missions);
            return final_missions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private List<Mission> getTopMissions(Map<Mission, Double> missionScores, int difficulty) {
        List<Map.Entry<Mission, Double>> sortedMissions = new ArrayList<>(missionScores.entrySet());

        Collections.sort(sortedMissions, new Comparator<Map.Entry<Mission, Double>>() {
            @Override
            public int compare(Map.Entry<Mission, Double> e1, Map.Entry<Mission, Double> e2) {
                return Double.compare(e2.getValue(), e1.getValue());
            }
        });

        List<Mission> topMissions = new ArrayList<>();
        int limit = (difficulty == 0) ? 2 : 1;
        for (int i = 0; i < limit && i < sortedMissions.size(); i++) {
            topMissions.add(sortedMissions.get(i).getKey());
        }
        return topMissions;
    }

}
