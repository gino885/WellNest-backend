package com.wellnest.comic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.util.api.OpenAiHttp;
import com.wellnest.comic.dao.CollectionDao;
import com.wellnest.comic.dao.ComicRepo;
import com.wellnest.comic.model.ChatData;
import com.wellnest.comic.model.Collection;
import com.wellnest.comic.model.Comic;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

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
        chatDao.saveTitle(chatId, title.replaceAll("\\*", ""));
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

            Map<String, List<?>> urlsByType = new HashMap<>();
            urlsByType.put("comic", createComicData(comicUrls));
            urlsByType.put("voice", splitUrls(voiceUrls));

            List<Map<String, String>> dialogues = createDialoguesForChat((Integer) result[4]);

            ChatData chatData = new ChatData(title, urlsByType, date, dialogues);
            chatDataList.add(chatData);
        }

        return chatDataList;
    }
    private List<Map<String, String>> createDialoguesForChat(Integer chatId) {
        List<Object[]> voiceDialogues = collectionDao.getVoiceDialogues(chatId);
        List<Map<String, String>> dialogues = new ArrayList<>();

        for (Object[] dialogueResult : voiceDialogues) {
            Integer page = (Integer) dialogueResult[0];
            String content = (String) dialogueResult[1];

            Map<String, String> dialogue = new HashMap<>();
            dialogue.put("page", String.valueOf(page));
            dialogue.put("content", content);
            dialogues.add(dialogue);
        }

        return dialogues;
    }

    private List<Map<String, String>> createComicData(String comicUrls) {
        List<Map<String, String>> comicDataList = new ArrayList<>();
        if (comicUrls == null || comicUrls.isEmpty()) {
            return comicDataList;
        }

        String[] urlArray = comicUrls.split(",");
        for (String url : urlArray) {
            Map<String, String> comicData = new HashMap<>();
            comicData.put("url", url.trim());
            comicData.put("caption", collectionDao.getCaptionByUrl(url));
            comicDataList.add(comicData);
        }

        return comicDataList;
    }

    private List<String> splitUrls(String urls) {
        if (urls == null || urls.isEmpty()) {
            return new ArrayList<>();
        }
        String[] urlArray = urls.split(",");
        return Arrays.stream(urlArray).map(String::trim).collect(Collectors.toList());
    }

}


