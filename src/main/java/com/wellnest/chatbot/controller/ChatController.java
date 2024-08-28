package com.wellnest.chatbot.controller;

import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.dto.MessageRequeat;
import com.wellnest.chatbot.service.ChatService;
import com.wellnest.user.dto.UserRegisterRequest;
import com.wellnest.user.model.User;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.validation.Valid;
import java.security.Key;
import java.util.Base64;

@RestController
public class ChatController {
    @Autowired
    private ChatService chatService;
    String secretKey = System.getenv("JWT_SECRET_KEY");
    private Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));


    @PostMapping("/chat/create")
    public ResponseEntity<?> createChat(@RequestHeader("Authorization") String authToken,
                                        @RequestBody ChatCreateRequest chatCreateRequest) {
        if (authToken != null && authToken.startsWith("Bearer ")) {
            String token = authToken.substring(7);
            String userId = getUserIdFromToken(token);


            chatCreateRequest.setUserId(userId);

            chatService.createChat(chatCreateRequest);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token is missing or not valid.");
        }
    }
    @PostMapping("/message/create")
    public ResponseEntity<?> createMessage(@RequestHeader("Authorization") String authToken,
                                           @RequestBody MessageRequeat messageRequeat) {
        if (authToken != null && authToken.startsWith("Bearer ")) {
            String token = authToken.substring(7);
            String userId = getUserIdFromToken(token);

            messageRequeat.setChatId(chatService.getChatId(Integer.parseInt(userId)));
            messageRequeat.setUserId(Integer.parseInt(userId));

            chatService.createMessage(messageRequeat);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token is missing or not valid.");
        }
    }

    @PostMapping("/chat/finish")
    public ResponseEntity<?> finishChat(@RequestHeader("Authorization") String authToken,
                                        @RequestBody ChatCreateRequest chatCreateRequest) {
        if (authToken != null && authToken.startsWith("Bearer ")) {
            String token = authToken.substring(7);
            String userId = getUserIdFromToken(token);

            MessageRequeat messageRequeat = new MessageRequeat();
            messageRequeat.setContent(chatCreateRequest.getContent());
            messageRequeat.setChatId(chatService.getChatId(Integer.parseInt(userId)));
            messageRequeat.setUserId(Integer.parseInt(userId));
            chatCreateRequest.setUserId(userId);
            chatService.createMessage(messageRequeat);
            chatService.finishChat(chatCreateRequest);


            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token is missing or not valid.");
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
