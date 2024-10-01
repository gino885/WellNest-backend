package com.wellnest.comic.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.util.api.OpenAiHttp;
import com.wellnest.comic.model.ChatData;
import com.wellnest.comic.model.Dialogue;
import com.wellnest.comic.service.ChatTTSService;
import com.wellnest.comic.service.CollectionService;
import com.wellnest.comic.service.ComicService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Key;
import java.util.*;

@RestController
@RequestMapping("/comic")
@Slf4j
@EnableAsync
public class ComicController {

    @Autowired
    private ComicService comicService;
    @Autowired
    private ChatTTSService chatTTSService;
    @Autowired
    private OpenAiHttp openAiHttp;

    @Autowired
    private ChatDao chatDao;

    @Autowired
    private CollectionService collectionService;

    String secretKey = System.getenv("JWT_SECRET_KEY");
    private Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    private static final Logger logger = LoggerFactory.getLogger(ComicController.class);

    @GetMapping
    public ResponseEntity<?> generateComic(@RequestHeader("Authorization") String authToken) throws Exception {
        try {
            String userId;
            if (authToken != null && authToken.startsWith("Bearer ")) {
                String token = authToken.substring(7);
                userId = getUserIdFromToken(token);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token is missing or not valid.");
            }
            System.out.println(chatDao.getStatusById(Integer.parseInt(userId)));
            String messages = String.join(" ", chatDao.getMessagebyUserId(Integer.parseInt(userId)));
            Integer chatId = chatDao.getChatId(Integer.parseInt(userId));
            String mission = chatDao.getMissionById(chatId);
            chatDao.finishChat(userId, "generated");
            String description = openAiHttp.getChatCompletion(messages, mission ,"description");
            System.out.println("description: " + description);

            String caption = openAiHttp.getChatCompletion(description, null, "caption");
            String narration = openAiHttp.getChatCompletion(description, messages, "narration");
            String title = collectionService.getTitle(description, messages, chatId);

            String[] captions = caption.replace("caption: [", "").replace("]", "").trim().split(",");
            for (int i = 0; i < captions.length; i++) {
                captions[i] = captions[i].trim();
            }
            System.out.println("caption" + caption);
            System.out.println("narration: " + narration);
            String[] narration_sep = narration.split("\n");
            List<Dialogue> dialogues = new ArrayList<>();
            for (String narative : narration_sep){
                if(narative.startsWith("[Dialogue")){
                    Character sceneNum = narative.charAt(narative.indexOf("_")+1);
                    String content = narative.substring(narative.indexOf("]") + 2)
                            .replace("[uv_break]", "")
                            .replace("[laugh]", "");
                    Dialogue dialogue = new Dialogue(sceneNum, content);
                    dialogues.add(dialogue);
                }
            }
            List<String> imageUrls = comicService.generateComic(description,chatId, userId, captions);

            List<String> audioList = chatTTSService.processNarrationAndDialogue(narration, chatId, userId);

            log.info("Images and narration processing completed.");
            Date date = new Date();
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("comic", imageUrls);
            responseMap.put("audio", audioList);
            responseMap.put("dialogues", dialogues);
            responseMap.put("captions", captions);
            responseMap.put("title", title);
            responseMap.put("date", date);

            String bgmPath = "https://wellnestbucket.s3.ap-southeast-2.amazonaws.com/cozy_bgm.mp3";
            responseMap.put("bgm", bgmPath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.ok().headers(headers).body(responseMap);

        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }

    }

    @GetMapping("/collection")
    public ResponseEntity<?> getCollection(@RequestHeader("Authorization") String authToken) throws Exception{
        try{
            String userId;
            if (authToken != null && authToken.startsWith("Bearer ")) {
                String token = authToken.substring(7);
                userId = getUserIdFromToken(token);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token is missing or not valid.");
            }
            List<ChatData> chatData = collectionService.getCollection(Integer.parseInt(userId));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            System.out.println(chatData);
            return ResponseEntity.ok().headers(headers).body(chatData);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( e.getMessage());
        }

    }
    private String getUserIdFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}


