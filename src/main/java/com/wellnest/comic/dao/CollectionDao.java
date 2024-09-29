package com.wellnest.comic.dao;

import com.wellnest.chatbot.model.Chat;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionDao extends CrudRepository<Chat, Long> {

    @Query(value = "SELECT " +
            "ch.title, " +
            "GROUP_CONCAT(CASE WHEN c.type = 'comic' THEN c.url END) AS comicUrls, " +
            "GROUP_CONCAT(CASE WHEN c.type = 'voice' THEN c.url END) AS voiceUrls, " +
            "ch.date " +
            "FROM collection c " +
            "JOIN chat ch ON c.chat_id = ch.chat_id " +
            "WHERE ch.user_id =:userId " +
            "GROUP BY ch.chat_id", nativeQuery = true)
    List<Object[]> getUrlsGroupedByType(@Param("userId" ) Integer userId);
}
