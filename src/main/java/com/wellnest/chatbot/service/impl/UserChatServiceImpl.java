package com.wellnest.chatbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.wellnest.chatbot.enmus.MessageType;
import com.wellnest.chatbot.enmus.UserType;
import com.wellnest.chatbot.listener.OpenAISubscriber;
import com.wellnest.chatbot.service.UserChatService;
import com.wellnest.chatbot.service.dto.Message;
import com.wellnest.chatbot.util.api.OpenAiWebClient;
import com.wellnest.chatbot.util.session.UserSessionUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author niuxiangqian
 * @version 1.0
 * @date 2023/3/23 14:52
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class UserChatServiceImpl implements UserChatService {
    private final UserSessionUtil userSessionUtil;
    private final OpenAiWebClient openAiWebClient;
    /**
     * tokens和中文的转化比例
     */
    private static final float TOKEN_CONVERSION_RATE = 0.7f;
    /**
     * 最长tokens
     */
    private static final Integer MAX_TOKEN = 4096;
    /**
     * 最大中文长度
     */
    private static final Integer CHINESE_LENGTH = (int) (MAX_TOKEN / TOKEN_CONVERSION_RATE);
    private static final List<String> IMAGE_COMMAND_PREFIX = Arrays.asList("画", "找");

    @Override
    public Flux<String> send(MessageType type, String content, String sessionId) {

            userSessionUtil.initializeHistory(sessionId);

        Message userMessage = new Message(MessageType.TEXT, UserType.USER, content);
        int currentToken = (int) (content.length() / TOKEN_CONVERSION_RATE);

        List<Message> history = userSessionUtil.getHistory(sessionId, MessageType.TEXT, (int) (CHINESE_LENGTH - currentToken));
        log.info("history:{}", history);
        String historyDialogue = history.stream().map(e -> String.format(e.getUserType().getCode(), e.getMessage())).collect(Collectors.joining());

        String prompt = StringUtils.hasLength(historyDialogue) ? String.format("%sQ:%s\nA: ", historyDialogue, content) : content;

        log.info("prompt:{}", prompt);
        return Flux.create(emitter -> {
            OpenAISubscriber subscriber = new OpenAISubscriber(emitter, sessionId, this, userMessage);
            Flux<String> openAiResponse =
                    openAiWebClient.getChatResponse(sessionId, prompt, null, null, null);
            openAiResponse.subscribe(subscriber);
            emitter.onDispose(subscriber);
        });
    }

    @Override
    public List<Message> getHistory(String sessionId) {
        return userSessionUtil.getHistory(sessionId, null, Integer.MAX_VALUE);
    }

    @Override
    public void completed(Message questions, String sessionId, String response) {
        userSessionUtil.addMessage(sessionId, questions);
        userSessionUtil.addMessage(sessionId, new Message(questions.getMessageType(), UserType.BOT, response));
    }

    @Override
    public void fail(Message questions, String sessionId, String errorMsg) {
        log.error("openai 处理失败 sessionId:{},questions:{},errorMsg:{}", sessionId, questions, errorMsg);
    }

    @Override
    public void clearHistory(String sessionId) {
        log.info("清除历史记录 sessionId:{}", sessionId);
        userSessionUtil.clearHistory(sessionId);
    }

    public void getRespoond (String prompt){
        openAiWebClient.getChatResponse(null, prompt, null, null, null);
    }

}
