package com.wellnest.chatbot.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class ChatCreateRequest {
    @NotBlank
    private String uuid;

    private String status;
    private String content;
    private String userId;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
