package com.wellnest.Dao.impl;

import com.wellnest.Dao.UserDao;
import com.wellnest.RowMapper.UserRowMapper;
import com.wellnest.dto.UpdateProfileRequest;
import com.wellnest.dto.UserRegisterRequest;
import com.wellnest.handleException.UserCreationException;
import com.wellnest.model.Chat;
import com.wellnest.model.User;
import com.wellnest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import com.wellnest.RowMapper.ChatRowMapper;

import java.util.*;

@Component
public class UserDaoimpl implements UserDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public User getUserById(Integer userId) {
        String sql = "SELECT userId, email, password, name, createdDate, lastModifiedDate " +
                "FROM user WHERE userId = :userId";

        Map<String,Object> map = new HashMap<>();
        map.put("userId", userId);

        List<User> userList = namedParameterJdbcTemplate.query(sql, map, new UserRowMapper());

        if(userList.size() > 0) {
            return userList.get(0);
        }
        else{
            return null;
        }
    }

    @Override
    public User getUserByEmail(String email) {

    	String sql = "SELECT userId, email, password, name, gender, age, Avatar_num, createdDate, lastModifiedDate " +
                "FROM user WHERE email = :email";

        Map<String,Object> map = new HashMap<>();
        map.put("email", email);

        List<User> userList = namedParameterJdbcTemplate.query(sql, map, new UserRowMapper());

        if(userList.size() > 0) {
            return userList.get(0);
        }
        else{
            return null;
        }
    }

    public Integer createUser(UserRegisterRequest userRegisterRequest) {
        String sql = "INSERT INTO user(Name, Email, Password, Gender, Age, Avatar_num, CreatedDate, LastModifiedDate) " +
                     "VALUES (:name, :email, :password, :gender, :age, :avatarNum, :createdDate, :lastModifiedDate)";

        Map<String, Object> map = new HashMap<>();
        map.put("name", userRegisterRequest.getName());
        map.put("email", userRegisterRequest.getEmail());
        map.put("password", userRegisterRequest.getPassword());
        map.put("status", userRegisterRequest.getStatus()); // Added status
        map.put("gender", userRegisterRequest.getGender()); // Added gender
        map.put("age", userRegisterRequest.getAge());       // Added age
        map.put("avatarNum", userRegisterRequest.getAvatarNum()); // Added avatarNum

        Date now = new Date();
        map.put("createdDate", now);
        map.put("lastModifiedDate", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        // Assuming keyHolder will have a key here, otherwise you might need additional error handling
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.intValue();
        } else {
            // Handle the case where keyHolder did not return a key
            throw new UserCreationException("Unable to retrieve generated key for user.");
        }
    }
    
    public void saveUserChatThread(Integer userId, String threadId) {
        String sql = "INSERT INTO Chat (UserId, ThreadID) VALUES (:userId, :threadId)";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("threadId", threadId);
        namedParameterJdbcTemplate.update(sql, params);
    }
    
    @Override
    public List<Chat> getChatsByUserId(Integer userId) {
        String sql = "SELECT * FROM Chat WHERE UserID = :userId";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return namedParameterJdbcTemplate.query(sql, params, new ChatRowMapper());
    }




    public boolean editProfile(UpdateProfileRequest updateProfileRequest) {
        String sql = "UPDATE user SET ";

        Map<String, Object> paramMap = new HashMap<>();

        // Check and add parameters to update
        if (updateProfileRequest.getName() != null) {
            sql += "name = :name, ";
            paramMap.put("name", updateProfileRequest.getName());
        }
        if (updateProfileRequest.getPassword() != null) {
            sql += "password = :password, ";
            paramMap.put("password", updateProfileRequest.getPassword());
        }
        if (updateProfileRequest.getNickName() != null) {
            sql += "nickName = :nickName, ";
            paramMap.put("nickName", updateProfileRequest.getNickName());
        }
        if (updateProfileRequest.getCountry() != null) {
            sql += "country = :country, ";
            paramMap.put("country", updateProfileRequest.getCountry());
        }
        Date date = new Date();
        sql += "lastModifiedDate = :lastModifiedDate, ";
        paramMap.put("lastModifiedDate", date);
        // Remove the last comma and space
        sql = sql.substring(0, sql.length() - 2);

        // Add WHERE clause
        sql += " WHERE email = :email";
        paramMap.put("email", updateProfileRequest.getEmail());

        // Perform the update
        int updated = namedParameterJdbcTemplate.update(sql, paramMap);

        // Return true if the update was successful
        return updated > 0;
    }


}
