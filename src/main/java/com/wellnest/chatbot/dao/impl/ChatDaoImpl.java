package com.wellnest.chatbot.dao.impl;

import com.wellnest.chatbot.dao.ChatDao;
import com.wellnest.chatbot.dto.ChatCreateRequest;
import com.wellnest.chatbot.dto.MessageRequeat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class ChatDaoImpl implements ChatDao {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Override
    public Integer createChat(ChatCreateRequest chatCreateRequest) {
        String sql = "INSERT INTO chat(date, status, user_id)" +
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

    @Override
    public Integer createMessage(MessageRequeat messageRequeat) {
        String sql = "INSERT INTO message(date, content, user_id, chat_id)" +
                "VALUES (:date, :content, :userId, :chatId)";

        Map<String, Object> map = new HashMap<>();
        map.put("userId", messageRequeat.getUserId());
        map.put("content", messageRequeat.getContent());
        map.put("chatId",messageRequeat.getChatId());

        Date now = new Date();
        map.put("date", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        int chatId = keyHolder.getKey().intValue();

        return chatId;
    }

    @Override
    public Integer getChatId(Integer userId) {
        String countSql = "SELECT COUNT(*) FROM chat WHERE user_id = :userId AND status = :status";

        Map<String, Object> countParams = new HashMap<>();
        countParams.put("userId", userId);
        countParams.put("status", "created");

        int createdCount = namedParameterJdbcTemplate.queryForObject(countSql, countParams, Integer.class);

        if (createdCount > 1) {
            throw new IllegalStateException("More than one chat with status 'created' exists for the user with ID: " + userId);
        }

        String sql = "SELECT chat_id FROM chat WHERE user_id = :userId AND status = :status";

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, countParams, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void finishChat(ChatCreateRequest chatCreateRequest) {
        String sql = "UPDATE chat SET status = :status, date = :date WHERE chat_id = :chatId";

        Map<String, Object> map = new HashMap<>();
        map.put("chatId", getChatId(Integer.parseInt(chatCreateRequest.getUserId())));
        map.put("status", "completed");

        Date now = new Date();
        map.put("date", now);

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map));
    }

    @Override
    public List<String> getMessagebyId(Integer chatId) {
        String sql = "SELECT content FROM message WHERE chat_id = :chatId";

        Map<String, Object> countParams = new HashMap<>();
        countParams.put("chatId", chatId);

        try {
            return namedParameterJdbcTemplate.queryForList(sql, countParams, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
