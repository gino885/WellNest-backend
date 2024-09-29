package com.wellnest.chatbot.dao;


import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.dto.MessageRequeat;
import com.wellnest.chatbot.model.Chat;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.List;

public interface ChatDao {
    Integer createChat(ChatCreateRequest chatCreateRequest);
    Integer createMessage(MessageRequeat messageRequeat);

    Integer getChatId(Integer userId);

    void finishChat(String userId, String status);

    String getStatusById(Integer userId);

    List<String> getMessagebyId(Integer chatId);

    List<String> getMessagebyUserId(Integer userId);

    void saveTitle(Integer chatId, String title);

    void storeMission(Integer chatId, String mission);

    String getMissionById(Integer chatId);
}
