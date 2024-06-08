package com.wellnest.chatbot.service;

import com.wellnest.chatbot.dto.ScaleCreateRequest;

public interface ScaleService {
    Integer createScale(ScaleCreateRequest scaleCreateRequest);
}
