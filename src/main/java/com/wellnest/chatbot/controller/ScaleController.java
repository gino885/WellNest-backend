package com.wellnest.chatbot.controller;

import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.dto.ScaleCreateRequest;
import com.wellnest.chatbot.service.ChatService;
import com.wellnest.chatbot.service.ScaleService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Base64;
@RestController
@RequestMapping("scales")
public class ScaleController {
    @Autowired
    private ScaleService scaleService;
    String secretKey = System.getenv("JWT_SECRET_KEY");
    private Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));


    @PostMapping
    public ResponseEntity<?> createScale(@RequestHeader("Authorization") String authToken,
                                        @RequestBody ScaleCreateRequest scaleCreateRequest) {
        if (authToken != null && authToken.startsWith("Bearer ")) {
            String token = authToken.substring(7);
            String userId = getUserIdFromToken(token);

            scaleCreateRequest.setUserId(userId);

            scaleService.createScale(scaleCreateRequest);

            return ResponseEntity.status(HttpStatus.CREATED).build();
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
