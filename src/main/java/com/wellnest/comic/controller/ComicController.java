package com.wellnest.comic.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.util.api.OpenAiHttp;
import com.wellnest.chatbot.util.api.OpenAiWebClient;
import com.wellnest.comic.model.Comic;
import com.wellnest.comic.model.ComicRequest;
import com.wellnest.comic.service.ChatTTSService;
import com.wellnest.comic.service.CollectionService;
import com.wellnest.comic.service.ComicService;
import com.wellnest.comic.service.ImageService;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/comic")
@Slf4j
@EnableAsync
public class ComicController {

    @Autowired
    private ComicService comicService;
    @Autowired
    private ImageService imageService;
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
            if( chatDao.getStatusById(Integer.parseInt(userId)).equals("generated")){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This Chat has already generated comic！");
            }
            String messages = String.join(" ", chatDao.getMessagebyUserId(Integer.parseInt(userId)));
            Integer chatId = chatDao.getChatId(Integer.parseInt(userId));
            chatDao.finishChat(userId, "generated");
            String description = openAiHttp.getChatCompletion(messages,null ,"description");
            System.out.println("description" + description);

            String caption = openAiHttp.getChatCompletion(description, null, "caption");
            String narration = openAiHttp.getChatCompletion(description, messages, "narration");
            System.out.println("caption" + caption);
            System.out.println("narration" + narration);
            List<String> imageUrls = comicService.generateComic(description,chatId, userId);
            List<String> imagePaths;
            try {
                //String withoutBrackets = caption.replace("[", "").replace("]", "");

               // String[] array = withoutBrackets.split(",\\s*");


                log.info("Images merged successfully.");
            } catch (Exception e) {
                log.error("Error during image merging: {}", e.getMessage());
                throw e;
            }

            List<String> audioList = chatTTSService.processNarrationAndDialogue(narration, chatId, userId);

            log.info("Images and narration processing completed.");

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("comic", imageUrls);
            responseMap.put("audio", audioList);
            String directoryPath = "src/voice";
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
            String result = collectionService.getCollection(Integer.parseInt(userId));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.ok().headers(headers).body(result);
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


