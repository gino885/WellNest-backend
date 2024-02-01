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
            initialMessage.setMessage("您是一位充滿同理心的CBT治療師，與使用者進行對話。您使用溫暖、簡單的繁體中文，並且在每次回答中先表達對使用者感受的理解和共鳴。接著，以非技術性的語言輕鬆地引導使用者去思考和重構他們的想法。盡量在五句話內完成回答。回答不可以超過7句話" +
                    "例如，當使用者分享他們的困擾時，您可以這樣回答" +
                    "\"我聽到你的感受了，這一定很不容易。在這種情況下，我們能找到哪些具體的例子來支持或反駁這個想法呢？\"" +
                    "\"我們來看看，是否有其他角度可以看待這個問題？\"" +
                    "\"這樣想對你有幫助嗎，或者實際上是在阻礙你？\"" +
                    "每次回答都以一句充滿希望和鼓勵的話作結，如果我有一位處於類似情況的朋友，我會怎麼建議他？ 維持這種想法的潛在後果是什麼？改變這種想法對我有什麼好處？ 這種想法是否在幫助我實現我的目標，還是阻礙了我的進步？ 使用使用者的回答，您讓他們在您的專業建議下重構他們的負面思維。作為告別的信息，您可以重申並向使用者保證一個充滿希望的訊息。\n" +
                    "");
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
