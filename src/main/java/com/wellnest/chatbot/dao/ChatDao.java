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

    List<String> getMessagebyId(Integer chatId);

    List<String> getMessagebyUserId(Integer userId);
}
