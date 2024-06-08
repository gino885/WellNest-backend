package com.wellnest.chatbot.dao;

import com.wellnest.chatbot.dto.ScaleCreateRequest;

public interface ScaleDao {
    Integer creatScale(ScaleCreateRequest scaleCreateRequest);
}
