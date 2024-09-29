package com.wellnest.comic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.util.api.OpenAiHttp;
import com.wellnest.comic.dao.CollectionDao;
import com.wellnest.comic.dao.ComicRepo;
import com.wellnest.comic.model.ChatData;
import com.wellnest.comic.model.Collection;
import com.wellnest.comic.model.Comic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CollectionService {
    @Autowired
    private OpenAiHttp openAiHttp;
    @Autowired
    private ComicRepo comicRepo;
    @Autowired
    private ChatDao chatDao;
    @Autowired
    private CollectionDao collectionDao;
    public String getTitle(String description, String messages, Integer chatId) throws Exception{
        String title = openAiHttp.getChatCompletion(description, messages, "title");
        chatDao.saveTitle(chatId, title);
        return title;
    }

    public List<ChatData> getCollection(Integer userId) {
        List<Object[]> results = collectionDao.getUrlsGroupedByType(userId);
        List<ChatData> chatDataList = new ArrayList<>();

        for (Object[] result : results) {
            String title = (String) result[0];
            String comicUrls = (String) result[1];
            String voiceUrls = (String) result[2];
            Date date = (Date) result[3];

            Map<String, List<String>> urlsByType = new HashMap<>();
            urlsByType.put("comic", splitUrls(comicUrls));
            urlsByType.put("voice", splitUrls(voiceUrls));

            ChatData chatData = new ChatData(title, urlsByType, date);
            chatDataList.add(chatData);
        }

        return chatDataList;
    }

    private List<String> splitUrls(String urls) {
        if (urls == null || urls.isEmpty()) {
            return new ArrayList<>();
        }
        String[] urlArray = urls.split(",");
        List<String> urlList = new ArrayList<>();
        for (String url : urlArray) {
            urlList.add(url.trim());
        }
        return urlList;
    }
}


