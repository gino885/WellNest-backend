package com.wellnest.controller;

import com.wellnest.dto.UpdateProfileRequest;
import com.wellnest.dto.UserLoginRequest;
import com.wellnest.dto.UserRegisterRequest;
import com.wellnest.model.User;
import com.wellnest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<User> login(@RequestBody @Valid UserLoginRequest userLoginRequest){
        User user = userService.login(userLoginRequest);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
    @PostMapping("users/edit")
    public ResponseEntity<Boolean> editProfile(@RequestBody @Valid UpdateProfileRequest updateProfileRequest){
        Boolean status = userService.editProfile(updateProfileRequest);

        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
}
