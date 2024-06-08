package com.wellnest.chatbot.dao.impl;

import com.wellnest.chatbot.dao.ScaleDao;
import com.wellnest.chatbot.dto.ScaleCreateRequest;
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
public class ScaleDaoImpl implements ScaleDao {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Override
    public Integer creatScale(ScaleCreateRequest scaleCreateRequest) {
        String sql = "INSERT INTO scale(date, score, user_id)" +
                "VALUES (:date, :score, :userId)";

        Map<String, Object> map = new HashMap<>();
        map.put("score", scaleCreateRequest.getScore());
        map.put("userId", scaleCreateRequest.getUserId());

        Date now = new Date();
        map.put("date", now);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

        int scaleId = keyHolder.getKey().intValue();

        return scaleId;
    }
}
