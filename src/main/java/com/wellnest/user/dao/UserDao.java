package com.wellnest.user.dao;

import com.wellnest.user.dto.UpdateProfileRequest;
import com.wellnest.user.dto.UserRegisterRequest;
import com.wellnest.user.model.User;

public interface UserDao {
    Integer createUser(UserRegisterRequest userRegisterRequest);
    User getUserById(Integer userId);
    User getUserByEmail(String email);
    boolean editProfile(UpdateProfileRequest updateProfileRequest);
}
