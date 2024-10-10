package com.wellnest.chatbot.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.*;

@Component
public class ChatDaoImpl implements ChatDao {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;
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
        countParams.put("status", "generated");

        int createdCount = namedParameterJdbcTemplate.queryForObject(countSql, countParams, Integer.class);

        if (createdCount > 1) {
            throw new IllegalStateException("More than one chat with status 'created' exists for the user with ID: " + userId);
        }

        String sql = "SELECT chat_id FROM chat WHERE user_id = :userId AND status != :status";

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, countParams, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void finishChat(String userId, String status) {
        String sql = "UPDATE chat SET status = :status, date = :date WHERE chat_id = :chatId";

        Map<String, Object> map = new HashMap<>();
        map.put("chatId", getChatId(Integer.parseInt(userId)));
        map.put("status", status);
        System.out.println("status" + status);
        Date now = new Date();
        map.put("date", now);

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map));
    }

    @Override
    public String getStatusById(Integer userId) {
        String sql = "SELECT status FROM chat WHERE chat_id = :chatId";

        Map<String, Object> params = new HashMap<>();
        params.put("chatId", getChatId(userId));

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
        } catch (EmptyResultDataAccessException e) {
            return "No existing messages";
        }
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

    @Override
    public List<String> getMessagebyUserId(Integer userId) {
        String sql = "SELECT content FROM message WHERE chat_id IN (SELECT chat_id FROM chat WHERE user_id = :userId AND status = :status)";

        Map<String, Object> countParams = new HashMap<>();
        countParams.put("userId", userId);
        countParams.put("status", "completed");
        try {
            return namedParameterJdbcTemplate.queryForList(sql, countParams, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    @Override
    public void saveTitle(Integer chatId, String title) {
        String sql = "UPDATE chat SET title = :title WHERE chat_id = :chatId";

        Map<String, Object> map = new HashMap<>();
        map.put("chatId", chatId);
        map.put("title", title);

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map));
    }

    @Override
    public void storeMission(Integer chatId, String mission) {
        String sql = "UPDATE chat SET mission = :mission WHERE chat_id = :chatId";

        Map<String, Object> map = new HashMap<>();
        map.put("chatId", chatId);
        map.put("title", mission);

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map));
    }

    @Override
    public String getMissionById(Integer chatId) {

        String sql = "SELECT mission FROM chat WHERE chat_id = :chatId";

        Map<String, Object> params = new HashMap<>();
        params.put("chatId", chatId);

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
        } catch (EmptyResultDataAccessException e) {
            return "No existing messages";
        }
    }

    @Override
    public List<String> getEmotionById(Integer chatId) {
        String sql = "SELECT emotion FROM chat WHERE chat_id = :chatId";

        Map<String, Object> params = new HashMap<>();
        params.put("chatId", chatId);

        try {
            String emotionJson = namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
            return objectMapper.readValue(emotionJson, List.class);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
