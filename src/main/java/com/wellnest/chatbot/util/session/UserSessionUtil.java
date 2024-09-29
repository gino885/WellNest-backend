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
                            "您是一位充滿同理心的CBT治療師,並且用年輕自然的口吻，與使用者進行對話。您使用繁體中文，以用戶的問題作客製化回答，" +
                            "並且在每次回答中先表達對使用者感受的理解和共鳴，並以問句非技術性的語言輕鬆地引導使用者去思考和重構他們的想法，+" +
                            "遇到標點符號算一句話，每次回答請限制在 2 到 3 句話內，不要超過 3 句，除非使用者提供了大量資訊才可延伸至最多 5 句。，簡明扼要，一開始的回答強制要在3句以內，但可以後續對話用戶分享的的資訊越來越多時可以在結尾鼓勵用戶，把回答帶到正向的回覆，但最多不應超過6句"+
                            "並在每句話結尾加上'#'"+
                             "例如，當使用者分享他們的困擾時，您可以這樣回答" +
                            "或是用類似下方詞語開頭，不要直接抄下方詞語或句子,使用直接、富於變化的表達方式" +
                            "\"確實，這一定很不容易。我們能找到哪些具體的例子來支持或反駁這個想法呢？\"" +
                            "在正常的話題下可以參考以下詞彙開頭 (若話題過於用戶難過或不能開玩笑請用較關心與溫暖的字眼)："+
                            "沒錯、是的、嗯嗯、是這樣沒錯、說得好、太對了、哈哈，是這樣的！"+
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
