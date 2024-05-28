package com.wellnest.chatbot.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Date;

public class ChatCreateRequest {

    private Date date;
    @NotBlank
    private String status;
    @NotBlank
    private String userId;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
