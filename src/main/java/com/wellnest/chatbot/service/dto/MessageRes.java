package com.wellnest.chatbot.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.wellnest.chatbot.enmus.MessageType;

/**
 * @author niuxiangqian
 * @version 1.0
 * @date 2023/3/23 14:48
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRes {
    /**
     * 消息类型
     */
    private MessageType messageType;
    /**
     * 消息内容
     */
    private String message;
    /**
     * 是否结束
     */
    private Boolean end = Boolean.FALSE;

}
