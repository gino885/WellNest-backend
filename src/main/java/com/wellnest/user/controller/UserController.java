package com.wellnest.user.controller;

import com.wellnest.user.dto.UpdateProfileRequest;
import com.wellnest.user.dto.UserLoginRequest;
import com.wellnest.user.dto.UserRegisterRequest;
import com.wellnest.user.model.User;
import com.wellnest.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

import javax.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    String secretKey = System.getenv("JWT_SECRET_KEY");
    private Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));


    @PostMapping("/users/register")
    public ResponseEntity<User> register(@RequestBody @Valid UserRegisterRequest userRegisterRequest){
        Integer userId = userService.register(userRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.getUserById(userId));
    }

    @PostMapping("users/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginRequest userLoginRequest){
        User user = userService.login(userLoginRequest);
        if (user != null) {
            String token = generateToken(String.valueOf(user.getUserId()));
            Map<String, String> tokenResponse = new HashMap<>();
            tokenResponse.put("token", token);
            return ResponseEntity.ok(tokenResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }
    @PostMapping("users/edit")
    public ResponseEntity<Boolean> editProfile(@RequestBody @Valid UpdateProfileRequest updateProfileRequest){
        Boolean status = userService.editProfile(updateProfileRequest);

        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
    public String generateToken(String userId) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + 3600000;
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }
}

