package com.wellnest.RowMapper;

import org.springframework.jdbc.core.RowMapper;
import com.wellnest.model.Chat;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatRowMapper implements RowMapper<Chat> {

    @Override
    public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        Chat chat = new Chat();
        chat.setChatId(rs.getInt("chatId"));
        chat.setThreadId(rs.getString("threadId"));
        chat.setDate(rs.getDate("date"));
        chat.setType(rs.getString("type"));
        chat.setContent(rs.getString("content"));

        // 注意: 以下代码假定您有一个Collection类和User类，并且它们都有一个id字段
        // 如果实际情况不是这样，您需要相应地修改这些部分

        // 设置Collection对象，如果相关列存在
        // 例如:
        // if (rs.getObject("collectionId") != null) {
        //     Collection collection = new Collection();
        //     collection.setId(rs.getInt("collectionId"));
        //     chat.setCollection(collection);
        // }

        // 设置User对象，如果相关列存在
        // 例如:
        // if (rs.getObject("userId") != null) {
        //     User user = new User();
        //     user.setUserId(rs.getInt("userId"));
        //     chat.setUser(user);
        // }

        return chat;
    }
}
