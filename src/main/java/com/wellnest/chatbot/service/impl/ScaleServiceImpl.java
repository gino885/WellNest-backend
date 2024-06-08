package com.wellnest.chatbot.service.impl;


import com.wellnest.chatbot.dao.ScaleDao;
import com.wellnest.chatbot.dto.ScaleCreateRequest;
import com.wellnest.chatbot.service.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScaleServiceImpl implements ScaleService {
    @Autowired
    ScaleDao scaleDao;
    @Override
    public Integer createScale(ScaleCreateRequest scaleCreateRequest) {

        return scaleDao.creatScale(scaleCreateRequest);
    }
}
