package com.wellnest.chatbot.dao.impl;

import com.wellnest.chatbot.dao.MissionDao;
import com.wellnest.chatbot.model.Mission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
@Repository
public class MissionDaoImpl implements MissionDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final class MissionMapper implements RowMapper<Mission> {
        @Override
        public Mission mapRow(ResultSet rs, int rowNum) throws SQLException {
            Mission mission = new Mission();
            mission.setMissionID(rs.getInt("mission_id"));
            mission.setContent(rs.getString("content"));
            mission.setDifficulty(rs.getInt("difficulty"));
            return mission;
        }
    }

    public List<Mission> findByDifficulty(int difficulty) {
        String sql = "SELECT * FROM mission WHERE difficulty = ?";

        return jdbcTemplate.query(sql, new Object[]{difficulty}, new MissionMapper());
    }
}
