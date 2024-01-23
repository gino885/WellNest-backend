package com.wellnest.chatbot.service;

import com.wellnest.chatbot.enmus.MessageType;
import com.wellnest.chatbot.listener.CompletedCallBack;
import com.wellnest.chatbot.service.dto.Message;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author niuxiangqian
 * @version 1.0
 * @date 2023/3/23 14:47
 **/
public interface UserChatService extends CompletedCallBack {

    /**
     * 发送消息
     *
     * @param type
     * @param content
     * @param sessionId
     */
    Flux<String> send(MessageType type, String content, String sessionId);

    /**
     * 消息历史
     *
     * @param sessionId
     * @return
     */
    List<Message> getHistory(String sessionId);


}
