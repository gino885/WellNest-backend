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
public class UserDaoImpl implements UserDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public User getUserById(Integer userId) {
        String sql = "SELECT user_id, email, password, name, created_date, last_modified_date, gender, age " +
                "FROM user WHERE user_id = :userId";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        List<User> userList = namedParameterJdbcTemplate.query(sql, map, new UserRowMapper());

        if (userList.size() > 0) {
            return userList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public User getUserByEmail(String email) {
        String sql = "SELECT user_id, email, password, name, created_date, last_modified_date, gender, age " +
                "FROM user WHERE email = :email";

        Map<String, Object> map = new HashMap<>();
        map.put("email", email);

        List<User> userList = namedParameterJdbcTemplate.query(sql, map, new UserRowMapper());

        if (userList.size() > 0) {
            return userList.get(0);
        } else {
            return null;
        }
    }

    public Integer createUser(UserRegisterRequest userRegisterRequest) {
        String sql = "INSERT INTO user (email, password, name, created_date, last_modified_date, gender, age) " +
                "VALUES (:email, :password, :name, :createdDate, :lastModifiedDate, :gender, :age)";

        Map<String, Object> map = new HashMap<>();
        map.put("email", userRegisterRequest.getEmail());
        map.put("password", userRegisterRequest.getPassword());
        map.put("name", userRegisterRequest.getName());
        map.put("gender", userRegisterRequest.getGender().name()); // Ensure gender is stored as a string
        map.put("age", userRegisterRequest.getAge());

        Date now = new Date();
        map.put("createdDate", now);
        map.put("lastModifiedDate", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        return keyHolder.getKey().intValue();
    }


    public boolean editProfile(UpdateProfileRequest updateProfileRequest) {
        String sql = "UPDATE user SET ";

        Map<String, Object> paramMap = new HashMap<>();

        if (updateProfileRequest.getName() != null) {
            sql += "name = :name, ";
            paramMap.put("name", updateProfileRequest.getName());
        }
        if (updateProfileRequest.getPassword() != null) {
            sql += "password = :password, ";
            paramMap.put("password", updateProfileRequest.getPassword());
        }
        if (updateProfileRequest.getGender() != null) {
            sql += "gender = :gender, ";
            paramMap.put("gender", updateProfileRequest.getGender().name());
        }
        if (updateProfileRequest.getAge() != null) {
            sql += "age = :age, ";
            paramMap.put("age", updateProfileRequest.getAge());
        }
        Date date = new Date();
        sql += "last_modified_date = :lastModifiedDate ";
        paramMap.put("lastModifiedDate", date);

        sql += "WHERE email = :email";
        paramMap.put("email", updateProfileRequest.getEmail());

        int updated = namedParameterJdbcTemplate.update(sql, paramMap);

        return updated > 0;
    }
}
