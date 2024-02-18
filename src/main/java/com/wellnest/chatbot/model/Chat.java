package com.wellnest.chatbot.model;

import java.util.Date;

public class Chat {
    private Integer ChatId;
    private String UUId;
    private String status;
    private String content;
    private Date date;
    private Integer userId;

    public Integer getChatId() {
        return ChatId;
    }

    public void setChatId(Integer chatId) {
        ChatId = chatId;
    }

    public String getUUId() {
        return UUId;
    }

    public void setUUId(String UUId) {
        this.UUId = UUId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
