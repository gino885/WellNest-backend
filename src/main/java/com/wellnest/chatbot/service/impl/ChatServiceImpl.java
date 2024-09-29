package com.wellnest.chatbot.service.impl;

import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.dto.MessageRequeat;
import com.wellnest.chatbot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatServiceImpl implements ChatService {
    @Autowired
    ChatDao chatDao;
    @Override
    public Integer createChat(ChatCreateRequest chatCreateRequest) {
        chatCreateRequest.setStatus("created");

        return chatDao.createChat(chatCreateRequest);
    }

    @Override
    public Integer createMessage(MessageRequeat messageRequeat) {
        return chatDao.createMessage(messageRequeat);
    }

    @Override
    public Integer getChatId(Integer userId) {
        return chatDao.getChatId(userId);
    }

    @Override
    public void finishChat(ChatCreateRequest chatCreateRequest) {

        chatDao.finishChat(chatCreateRequest.getUserId(), "completed");
    }

    @Override
    public void storeMission(Integer chatId, String mission) {
        chatDao.storeMission(chatId, mission);
    }
}
