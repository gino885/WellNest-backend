package com.wellnest.chatbot.dao;


import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.model.Chat;

public interface ChatDao {
    Integer creatChat(ChatCreateRequest chatCreateRequest);

}
