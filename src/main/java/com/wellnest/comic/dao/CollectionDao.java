package com.wellnest.comic.dao;

import com.wellnest.chatbot.model.Chat;
import com.wellnest.comic.model.Comic;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionDao extends CrudRepository<Comic, Long> {

    @Query(value = "SELECT " +
            "ch.title, " +
            "GROUP_CONCAT(CASE WHEN c.type = 'comic' THEN c.url END) AS comicUrls, " +
            "GROUP_CONCAT(CASE WHEN c.type = 'voice' THEN c.url END) AS voiceUrls, " +
            "ch.date, " +
            "ch.chat_id  " +
            "FROM collection c " +
            "JOIN chat ch ON c.chat_id = ch.chat_id " +
            "WHERE ch.user_id =:userId " +
            "GROUP BY ch.chat_id", nativeQuery = true)
    List<Object[]> getUrlsGroupedByType(@Param("userId" ) Integer userId);

    @Query(value = "SELECT " +
            "ch.title, " +
            "GROUP_CONCAT(CASE WHEN c.type = 'comic' THEN c.url END) AS comicUrls, " +
            "GROUP_CONCAT(CASE WHEN c.type = 'voice' THEN c.url END) AS voiceUrls, " +
            "ch.date, " +
            "ch.chat_id  " +
            "FROM collection c " +
            "JOIN chat ch ON c.chat_id = ch.chat_id " +
            "WHERE (:missionId IS NULL OR ch.mission = :missionId) " +
            "AND ch.share = true " +
            "GROUP BY ch.chat_id", nativeQuery = true)
    List<Object[]> getUrlsGroupedByMissionId(@Param("missionId") Integer missionId);

    @Query (value = "SELECT attribute from Comic where url =:url")
    String getCaptionByUrl(@Param("url") String url);

    @Query(value = "SELECT c.page, c.attribute FROM collection c WHERE c.chat_id = :chatId AND c.type = 'voice' AND c.attribute IS NOT NULL AND c.attribute != ''", nativeQuery = true)
    List<Object[]> getVoiceDialogues(@Param("chatId") Integer chatId);
}
