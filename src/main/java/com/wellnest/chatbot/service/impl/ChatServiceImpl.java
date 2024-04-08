package com.wellnest.chatbot.service.impl;

import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatServiceImpl implements ChatService {
    @Autowired
    ChatDao chatDao;
    @Override
    public Integer createChat(ChatCreateRequest chatCreateRequest) {
        if (chatCreateRequest.getStatus() == null) {
            chatCreateRequest.setStatus("created");
        }


        return chatDao.creatChat(chatCreateRequest);
    }
}
