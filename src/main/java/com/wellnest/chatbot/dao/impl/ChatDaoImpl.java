package com.wellnest.chatbot.dao.impl;

import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.dto.ChatCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Component
public class ChatDaoImpl implements ChatDao {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Override
    public Integer creatChat(ChatCreateRequest chatCreateRequest) {
        String sql = "INSERT INTO Chat(Date, Status, UserID)" +
                "VALUES (:date, :status, :userId)";

        Map<String, Object> map = new HashMap<>();
        map.put("status", chatCreateRequest.getStatus());
        map.put("userId", chatCreateRequest.getUserId());

        Date now = new Date();
        map.put("date", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        int chatId = keyHolder.getKey().intValue();

        return chatId;
    }
}
