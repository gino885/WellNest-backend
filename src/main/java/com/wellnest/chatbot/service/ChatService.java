package com.wellnest.chatbot.service;

import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.dto.MessageRequeat;
import jakarta.persistence.criteria.CriteriaBuilder;

public interface ChatService {
    Integer createChat(ChatCreateRequest chatCreateRequest);
    Integer createMessage(MessageRequeat messageRequeat);

    Integer getChatId(Integer userId);

    void finishChat(ChatCreateRequest chatCreateRequest);
}
