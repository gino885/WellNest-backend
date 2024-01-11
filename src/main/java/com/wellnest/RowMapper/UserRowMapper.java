package com.wellnest.RowMapper;

import com.wellnest.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("userId"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setName(resultSet.getString("name"));
        user.setCreatedDate(resultSet.getDate("createdDate"));
        user.setLastModifiedDate(resultSet.getDate("lastModifiedDate"));
        return user;
    }
}