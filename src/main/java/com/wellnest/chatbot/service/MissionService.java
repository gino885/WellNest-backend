package com.wellnest.chatbot.service;

import com.wellnest.chatbot.model.Mission;

import java.util.List;

public interface MissionService {
    List<Mission> getMissionsByEmotion(String emotion);
}
