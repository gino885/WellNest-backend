package com.wellnest.chatbot.dto;

import javax.validation.constraints.NotBlank;
import java.util.Date;

public class ScaleCreateRequest {
    private Date date;
    @NotBlank
    private String score;
    @NotBlank
    private String userId;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
