package com.wellnest.service.impl;

import com.wellnest.Dao.UserDao;
import com.wellnest.dto.UpdateProfileRequest;
import com.wellnest.dto.UserLoginRequest;
import com.wellnest.dto.UserRegisterRequest;
import com.wellnest.handleException.EmailAlreadyRegisteredException;
import com.wellnest.model.Chat;
import com.wellnest.model.User;
import com.wellnest.service.OpenAIService;
import com.wellnest.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserServiceimpl implements UserService {

    private final static Logger log = LoggerFactory.getLogger((UserServiceimpl.class)) ;

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private OpenAIService openAIService;

    @Override
    public User login(UserLoginRequest userLoginRequest) {
        User user = userDao.getUserByEmail(userLoginRequest.getEmail());

        if (user == null) {
            log.warn("該email {} 尚未註冊", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not registered");
        }

        String hashedPassword = DigestUtils.md5DigestAsHex(userLoginRequest.getPassword().getBytes());

        if (user.getPassword().equals(hashedPassword)) {
        	log.info("userid {}", user.getUserId());
            List<Chat> chats = userDao.getChatsByUserId(user.getUserId());
            log.warn("chats:{}", chats);
            user.setChats(chats);  // 将 Chat 列表设置到 User 对象中
            return user;
        } else {
            log.warn("email {} 的密碼不正確", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect password");
        }
    }


    @Override
    public Integer register(UserRegisterRequest userRegisterRequest) {
        //檢查註冊的email
        User user = userDao.getUserByEmail(userRegisterRequest.getEmail());

        if(user != null){
            log.warn("該email {} 已被註冊", userRegisterRequest.getEmail());

            throw new EmailAlreadyRegisteredException("該電子郵件已被註冊");
        }

        //使用 MD5 生成密碼的雜湊值
        String hashedPassword = DigestUtils.md5DigestAsHex(userRegisterRequest.getPassword().getBytes());
        userRegisterRequest.setPassword(hashedPassword);
        
        //create user
        Integer userId = userDao.createUser(userRegisterRequest);
        
        //create ThreadID
        String threadId = openAIService.createThread();
        
        //save threadID to chat table
        userDao.saveUserChatThread(userId, threadId);
        

        return userId;
    }

    @Override
    public User getUserById(Integer userId) {

        return userDao.getUserById(userId);
    }

    @Override
    public boolean editProfile(UpdateProfileRequest updateProfileRequest) {
        return userDao.editProfile(updateProfileRequest);

    }
}