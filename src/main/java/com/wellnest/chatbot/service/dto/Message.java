package com.wellnest.chatbot.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.wellnest.chatbot.enmus.MessageType;
import com.wellnest.chatbot.enmus.UserType;

import java.util.Date;

/**
 * @author niuxiangqian
 * @version 1.0
 * @date 2023/3/23 14:48
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private MessageType messageType;
    private UserType userType;
    private String message;
    private Date date;

    public Message(MessageType messageType, UserType userType, String message) {
        this.messageType = messageType;
        this.userType = userType;
        this.message = message;
        this.date = new Date();
    }
}
