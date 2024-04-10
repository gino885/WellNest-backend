package com.wellnest.user.rowMapper;

import com.wellnest.user.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id")); // Adjusted from "userId" to "user_id"
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setName(resultSet.getString("name"));
        user.setCreatedDate(resultSet.getTimestamp("created_date")); // Adjusted from "createdDate" to "created_date"
        user.setLastModifiedDate(resultSet.getTimestamp("last_modified_date")); // Adjusted from "lastModifiedDate" to "last_modified_date"
        return user;
    }
}