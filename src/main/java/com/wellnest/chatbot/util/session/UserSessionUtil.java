package com.wellnest.chatbot.util.session;

import com.wellnest.chatbot.enmus.MessageType;
import com.wellnest.chatbot.enmus.UserType;
import com.wellnest.chatbot.service.dto.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author niuxiangqian
 * @version 1.0
 * @date 2023/3/23 14:47
 **/
@Component
public class UserSessionUtil {

    private static final Map<String, List<Message>> MESSAGE_HISTORY = new ConcurrentHashMap<>();

    /**
     * 消息列表
     *
     * @param sessionId
     * @param message
     */
    public void addMessage(String sessionId, Message message) {
        List<Message> messageList = MESSAGE_HISTORY.getOrDefault(sessionId, new ArrayList<>());
        messageList.add(message);
        MESSAGE_HISTORY.put(sessionId,messageList);
    }

    /**
     * 消息历史
     *
     * @param sessionId
     * @param messageType
     * @param maxTokens
     * @return
     */
    public List<Message> getHistory(String sessionId, MessageType messageType, Integer maxTokens) {

        List<Message> history = MESSAGE_HISTORY.getOrDefault(sessionId, new ArrayList<>());
        List<Message> result = new ArrayList<>();
        int count = 0;
        for (int i = history.size() - 1; i >= 0; i--) {
            Message message = history.get(i);
            if (messageType == null) {
                result.add(message);
                continue;
            }
            if (message.getMessageType() == messageType) {
                count += message.getMessage().length();
                if (count >= maxTokens) {
                    break;
                }
                result.add(message);
            }
        }
        Collections.reverse(result);
        return result;
    }
    /**
    * @param sessionId
     * @return
             **/
    public List<Message> initializeHistory(String sessionId) {
        // 检查是否已有该用户的历史记录
        if (!MESSAGE_HISTORY.containsKey(sessionId)) {
            // 如果没有，创建并添加初始消息
            List<Message> initialHistory = new ArrayList<>();
            Message initialMessage = new Message();
            initialMessage.setUserType(UserType.BOT);
            initialMessage.setMessageType(MessageType.TEXT);
            initialMessage.setMessage("‘Tone: conversational, spartan, use less corporate jargon'，" +
                            "您是用戶的朋友,並且用年輕自然的口吻，與使用者進行對話。您使用繁體中文，以用戶的問題作客製化回答，" +
                            "遇到標點符號算一句話，在剛開始每次回答請限制在 2 到 3 句話內，不要超過 3 句，後續可延伸至最多 5 句。可以後續對話用戶分享的的資訊越來越多時可以在結尾鼓勵用戶，把回答帶到正向的回覆，但最多不應超過6句"+
                            "並在每句話結尾加上'#'");
            initialHistory.add(initialMessage);

            MESSAGE_HISTORY.put(sessionId, initialHistory);
        }

        // 返回用户的消息历史（包括初始消息）
        return MESSAGE_HISTORY.get(sessionId);
    }


    public void clearHistory(String sessionId) {
        MESSAGE_HISTORY.remove(sessionId);
    }
}
