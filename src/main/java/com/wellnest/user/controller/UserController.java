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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.validation.Valid;


@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/users/register")
    public ResponseEntity<User> register(@RequestBody @Valid UserRegisterRequest userRegisterRequest){
        Integer userId = userService.register(userRegisterRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.getUserById(userId));
    }

    @PostMapping("users/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginRequest userLoginRequest){
        User user = userService.login(userLoginRequest);
        System.out.println(user.getEmail());
        System.out.println(user.getPassword());
        System.out.println(user.getUserId());

        if (user != null && user.getUserId() != null) { // 确保user不为null且userId也不为null
            String token = generateToken(String.valueOf(user.getUserId()));
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user_id", user.getUserId()); // 确保这里使用的是有效的userId
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }


    @PostMapping("users/edit")
    public ResponseEntity<Boolean> editProfile(@RequestBody @Valid UpdateProfileRequest updateProfileRequest){
        Boolean status = userService.editProfile(updateProfileRequest);

        return ResponseEntity.status(HttpStatus.OK).body(status);
    }

    // Example secret key - replace this with a securely generated key stored in a secure location
    private static final String SECRET_KEY = "8ac906886ef673c62634bddb4036df362b613edefb636e7c69dcf904f7a5353d";

    private String generateToken(String userId) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + 3600000; // Token有效期1小时
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY.getBytes()) // Use SECRET_KEY constant
                .compact();
    }
}
