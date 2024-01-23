package com.wellnest.user.service;

import com.wellnest.user.dto.UpdateProfileRequest;
import com.wellnest.user.dto.UserLoginRequest;
import com.wellnest.user.dto.UserRegisterRequest;
import com.wellnest.user.model.User;

public interface UserService {
    Integer register(UserRegisterRequest userRegisterRequest);
    User getUserById(Integer userId);
    User login(UserLoginRequest userLoginRequest);
    boolean editProfile(UpdateProfileRequest updateProfileRequest);
}
