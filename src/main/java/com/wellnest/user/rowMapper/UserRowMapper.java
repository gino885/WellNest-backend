package com.wellnest.user.rowMapper;

import com.wellnest.user.model.User;
import org.springframework.jdbc.core.RowMapper;
import com.wellnest.user.enmus.Gender;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setCreatedDate(rs.getTimestamp("created_date"));
        user.setLastModifiedDate(rs.getTimestamp("last_modified_date"));

        String genderStr = rs.getString("gender");
        if (genderStr != null) {
            user.setGender(Gender.valueOf(genderStr));
        } else {
            user.setGender(Gender.UNSET); // Assuming UNSET is an enum value for "未設定"
        }

        user.setAge(rs.getInt("age"));
        return user;
    }
}
