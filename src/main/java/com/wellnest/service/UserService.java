package com.wellnest.service;

import com.wellnest.dto.UserLoginRequest;
import com.wellnest.dto.UserRegisterRequest;
import com.wellnest.model.User;

public interface UserService {
    Integer register(UserRegisterRequest userRegisterRequest);
    User getUserById(Integer userId);
    User login(UserLoginRequest userLoginRequest);
}
