package com.wellnest.chatbot.controller;

import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.service.ChatService;
import com.wellnest.user.dto.UserRegisterRequest;
import com.wellnest.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
@RestController
public class ChatController {
    @Autowired
    private ChatService chatService;
    @PostMapping("/chat/create")
    public ResponseEntity<?> creatChat(@RequestHeader ("Authorization") String authToken,
                                       @RequestBody @Valid ChatCreateRequest chatCreateRequest) {
        if (authToken != null && authToken.startsWith("Bearer ")) {
            String token = authToken.substring(7); // 移除"Bearer "前綴
            String userId = getUserIdFromToken(token); // 解析Token以獲取userId
            chatCreateRequest.setUserId(userId);

            // 創建聊天記錄
            chatService.createChat(chatCreateRequest);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token is missing or not valid.");
        }
    }
    private String getUserIdFromToken(String token) {
        return Jwts.parser()
                .setSigningKey("userId")
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
