package com.wellnest.Dao;

import java.util.List;

import com.wellnest.dto.UpdateProfileRequest;
import com.wellnest.dto.UserRegisterRequest;
import com.wellnest.model.Chat;
import com.wellnest.model.User;

public interface UserDao {
    Integer createUser(UserRegisterRequest userRegisterRequest);
    User getUserById(Integer userId);
    User getUserByEmail(String email);
    boolean editProfile(UpdateProfileRequest updateProfileRequest);
	void saveUserChatThread(Integer userId, String threadId);
	List<Chat> getChatsByUserId(Integer userId);
}
