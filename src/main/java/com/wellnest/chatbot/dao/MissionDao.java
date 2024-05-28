package com.wellnest.chatbot.dao;

import com.wellnest.chatbot.model.Mission;

import java.util.List;

public interface MissionDao {
    Mission findByEmotionByDifficulty(String emotion, int difficulty);
}
