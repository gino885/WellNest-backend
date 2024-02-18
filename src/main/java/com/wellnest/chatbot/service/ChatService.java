package com.wellnest.chatbot.service;

import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.dto.ChatCreateRequest;

public interface ChatService {
    Integer createChat(ChatCreateRequest chatCreateRequest);
}
