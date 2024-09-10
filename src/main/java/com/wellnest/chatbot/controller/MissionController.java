package com.wellnest.chatbot.controller;

import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.model.Mission;
import com.wellnest.chatbot.service.MissionService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Base64;
import java.util.List;

@RestController
public class MissionController {

    @Autowired
    private MissionService missionService;

    String secretKey = System.getenv("JWT_SECRET_KEY");
    private Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    @Autowired
    private ChatDao chatDao;
    @GetMapping("mission")
    public List<Mission> getMission(@RequestHeader("Authorization") String authToken) {
        String userId;
        String token = authToken.substring(7);
        userId = getUserIdFromToken(token);
        String messages = String.join(" ", chatDao.getMessagebyUserId(Integer.parseInt(userId)));
        return missionService.getMission(messages);
    }
    private String getUserIdFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}