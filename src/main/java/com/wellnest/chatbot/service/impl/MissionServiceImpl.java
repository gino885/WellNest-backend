package com.wellnest.chatbot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.dao.MissionDao;
import com.wellnest.chatbot.model.Emotion;
import com.wellnest.chatbot.model.Mission;
import com.wellnest.chatbot.service.MissionService;
import com.wellnest.comic.model.ChatData;
import com.wellnest.comic.service.CollectionService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MissionServiceImpl implements MissionService {
    @Autowired
    private MissionDao missionDao;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private ChatDao chatDao;

    public List<Mission> getMission(String content, Integer userId) {
        try {
            OkHttpClient client = new OkHttpClient();
            System.out.println(content);

            String json = "{\"text\":\"" + content + "\"}";
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("https://194a-34-139-82-103.ngrok-free.app/predict")
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
            if (final_missions.size() < 3) {
                List<Mission> easyMissions = missionDao.findByDifficulty(0);
                int i = 0;
                while (final_missions.size() < 3 && i < easyMissions.size()) {
                    Mission candidateMission = easyMissions.get(i);
                    if (!final_missions.contains(candidateMission)) {
                        final_missions.add(candidateMission);
                    }
                    i++;
                }
            }
            List<ChatData> chatDataById = collectionService.getCollection(userId);
            int comicCount = 0;
            for (Mission mission : final_missions) {
                List<ChatData> chatDataList = collectionService.getCollectionByMission(mission.getMissionID());
                if (chatDataList == null || chatDataList.isEmpty()) {
                    if (chatDataById.get(comicCount) != null){
                        mission.setChatData(chatDataById.get(comicCount));
                        comicCount ++;
                    }
                } else {
                    ChatData bestChatData = null;
                    double highestScore = Double.NEGATIVE_INFINITY;

                    for (ChatData chatData : chatDataList) {
                        List<String> chatEmotions = chatDao.getEmotionById(chatData.getChatId());
                        double currentScore = 0;

                        for (String emotion : chatEmotions) {
                            currentScore += emotionScores.getOrDefault(emotion, 0.0);
                        }

                        if (currentScore > highestScore) {
                            highestScore = currentScore;
                            bestChatData = chatData;
                        }
                    }
                    mission.setChatData(bestChatData);
                }
            }
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
