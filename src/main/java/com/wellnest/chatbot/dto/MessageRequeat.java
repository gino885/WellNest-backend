package com.wellnest.chatbot.dto;

import jakarta.persistence.criteria.CriteriaBuilder;

import javax.validation.constraints.NotBlank;
import java.util.Date;

public class MessageRequeat {
    private Date date;
    @NotBlank
    private Integer userId;
    @NotBlank
    private Integer chatId;
    private String content;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
