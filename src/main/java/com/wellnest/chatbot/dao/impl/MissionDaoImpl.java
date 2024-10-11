package com.wellnest.chatbot.dao.impl;

import com.wellnest.chatbot.dao.MissionDao;
import com.wellnest.chatbot.model.Emotion;
import com.wellnest.chatbot.model.Mission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
@Repository
public class MissionDaoImpl implements MissionDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final class MissionMapper implements RowMapper<Mission> {
        private JdbcTemplate jdbcTemplate;

        // 通過構造函數注入 jdbcTemplate
        public MissionMapper(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }
        @Override
        public Mission mapRow(ResultSet rs, int rowNum) throws SQLException {
            Mission mission = new Mission();
            mission.setMissionID(rs.getInt("mission_id"));
            mission.setContent(rs.getString("content"));
            mission.setDifficulty(rs.getInt("difficulty"));

            String emotionSql = "SELECT e.* FROM emotion e JOIN mission_emotion me ON e.emotion_id = me.emotion_id WHERE me.mission_id = ?";
            List<Emotion> emotions = jdbcTemplate.query(emotionSql, new Object[]{mission.getMissionID()}, new EmotionMapper());
            mission.setEmotions(new HashSet<>(emotions));

            return mission;
        }
    }
    public static class EmotionMapper implements RowMapper<Emotion> {
        @Override
        public Emotion mapRow(ResultSet rs, int rowNum) throws SQLException {
            Emotion emotion = new Emotion();
            emotion.setId(rs.getInt("emotion_id"));
            emotion.setName(rs.getString("name"));
            return emotion;
        }
    }


    public List<Mission> findByDifficulty(int difficulty) {
        String sql = "SELECT * FROM mission WHERE difficulty = ?";

        return jdbcTemplate.query(sql, new Object[]{difficulty}, new MissionMapper(jdbcTemplate));
    }
}
