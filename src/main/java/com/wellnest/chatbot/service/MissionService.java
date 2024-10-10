package com.wellnest.chatbot.service;

import com.wellnest.chatbot.model.Mission;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.List;

public interface MissionService {
    List<Mission> getMission(String content, Integer userId);
}
