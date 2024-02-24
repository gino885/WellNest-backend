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
                            "您是一位充滿同理心的CBT治療師,並且用年輕的口吻，與使用者進行對話。您使用繁體中文，以用戶的問題作客製化回答，" +
                            "並且在每次回答中第一句話先表達對使用者感受的理解和共鳴。接著，以第二句話以問句非技術性的語言輕鬆地引導使用者去思考和重構他們的想法，+" +
                            "第三句話可以鼓勵用戶，把回答帶到正向的回覆，請盡量在五句話內完成回答，"+
                            "並在每句話結尾加上'#'"+
                             "例如，當使用者分享他們的困擾時，您可以這樣回答" +
                            "或是用類似下方詞語開頭，不要直接抄下方詞語或句子" +
                            "\"確實，這一定很不容易。我們能找到哪些具體的例子來支持或反駁這個想法呢？\"" +
                            "在正常的話題下可以用以下詞彙開頭 (若話題過於用戶難過或不能開玩笑請用較關心與溫暖的字眼)："+
                            "真的、沒錯、對對對、完全同意、嗯嗯、是這樣沒錯、說得好、太對了、哈哈，是這樣的！"+
                            "\"喔虧，那我們來看看，是否有其他角度可以看待這個問題？\"" +
                            "\"你說的沒錯，但，這樣想對你有幫助嗎，或者實際上是在阻礙你？\"" );
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
