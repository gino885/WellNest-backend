package com.wellnest.user.dao.impl;

import com.wellnest.user.dao.UserDao;
import com.wellnest.user.rowMapper.UserRowMapper;
import com.wellnest.user.dto.UpdateProfileRequest;
import com.wellnest.user.dto.UserRegisterRequest;
import com.wellnest.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

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

        String sql = "SELECT userId, email, password, name, createdDate, lastModifiedDate " +
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

    public Integer createUser(UserRegisterRequest userRegisterRequest){
        String sql = "INSERT INTO user(email, password, name, createdDate, lastModifiedDate) " +
                "VALUES (:email, :password, :name, :createdDate, :lastModifiedDate)";

        Map<String, Object> map = new HashMap<>();
        map.put("email", userRegisterRequest.getEmail());
        map.put("password", userRegisterRequest.getPassword());
        map.put("name", userRegisterRequest.getName());

        Date now = new Date();
        map.put("createdDate", now);
        map.put("lastModifiedDate", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        int userId = keyHolder.getKey().intValue();

        return userId;
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
