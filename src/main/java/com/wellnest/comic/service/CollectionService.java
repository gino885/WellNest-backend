package com.wellnest.comic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.comic.dao.ComicRepo;
import com.wellnest.comic.model.Comic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CollectionService {
    @Autowired
    private ComicRepo comicRepo;
    public String getCollection(Integer userId) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        List<Comic> comics = comicRepo.findByUserId(userId);

        Map<Integer, Map<String, List<Map<String, Object>>>> result = new HashMap<>();
        for (Comic comic : comics) {
            Integer chatId = comic.getChatId();
            String type = comic.getType();

            result.putIfAbsent(chatId, new HashMap<>());

            result.get(chatId).putIfAbsent(type, new ArrayList<>());

            Map<String, Object> comicData = new HashMap<>();
            comicData.put("url", comic.getUrl());
            comicData.put("page", comic.getPage());

            result.get(chatId).get(type).add(comicData);
        }

        return objectMapper.writeValueAsString(result);
    }

}
