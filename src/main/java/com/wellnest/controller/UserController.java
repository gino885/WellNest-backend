package com.wellnest.controller;

import com.wellnest.auth.JwtUtil;
import com.wellnest.dto.UpdateProfileRequest;
import com.wellnest.dto.UserLoginRequest;
import com.wellnest.dto.UserRegisterRequest;
import com.wellnest.model.Chat;
import com.wellnest.model.User;
import com.wellnest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    
    @PostMapping("/users/register")
    public ResponseEntity<Object> register(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        Integer userId = userService.register(userRegisterRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("userId", userId);

        // 使用HttpStatus.CREATED来表示资源已被成功创建
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/users/login")
    public ResponseEntity<Object> login(@RequestBody @Valid UserLoginRequest userLoginRequest){
        User user = userService.login(userLoginRequest);

        if (user != null) {
            // 用户验证成功
            String token = JwtUtil.generateToken(user.getName());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", user.getEmail()); // 添加email
            response.put("name", user.getName());   // 添加name
            response.put("age", user.getAge()); 
            response.put("gender", user.getGender()); 
            // 添加 threadId
            if (user.getChats() != null && !user.getChats().isEmpty()) {
                // 
                Chat chat = user.getChats().get(0);
                response.put("threadId", chat.getThreadId());
            } else {
                response.put("threadId", null); // 或者放入适当的默认值
            }
            
            return ResponseEntity.ok(response);
        } else {
            // 用户验证失败
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
        }
    }
    
    
    
    
    @PostMapping("users/edit")
    public ResponseEntity<Boolean> editProfile(@RequestBody @Valid UpdateProfileRequest updateProfileRequest){
        Boolean status = userService.editProfile(updateProfileRequest);

        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
}
