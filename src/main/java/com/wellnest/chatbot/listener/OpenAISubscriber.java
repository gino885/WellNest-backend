package com.wellnest.chatbot.listener;

import com.alibaba.fastjson.JSON;
import com.wellnest.chatbot.service.AzureSpeechService;
import com.wellnest.chatbot.service.UserChatService;
import com.wellnest.chatbot.service.impl.AzureSpeechServiceImpl;
import lombok.extern.slf4j.Slf4j;
import com.wellnest.chatbot.enmus.MessageType;
import com.wellnest.chatbot.service.dto.Message;
import com.wellnest.chatbot.service.dto.MessageRes;
import com.wellnest.chatbot.util.R;
import com.wellnest.chatbot.util.api.OpenAiWebClient;
import com.wellnest.chatbot.util.api.res.chat.image.DataRes;
import com.wellnest.chatbot.util.api.res.chat.image.OpenAiImageResponse;
import com.wellnest.chatbot.util.api.res.chat.text.OpenAiResponse;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;

import java.sql.SQLOutput;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * @author niuxiangqian
 * @version 1.0
 * @date 2023/3/21 20:15
 **/
@Slf4j
public class OpenAISubscriber implements Subscriber<String>, Disposable {
    private final FluxSink<String> emitter;
    private Subscription subscription;
    private final String sessionId;
    private final CompletedCallBack completedCallBack;
    private final StringBuilder sb;
    private final Message questions;
    private final MessageType messageType;
    private final StringBuilder sentence = new StringBuilder();
    private final StringBuilder audioSentnece = new StringBuilder();

    private AzureSpeechServiceImpl azureSpeechServiceimpl = new AzureSpeechServiceImpl();

    public OpenAISubscriber(FluxSink<String> emitter, String sessionId, CompletedCallBack completedCallBack, Message questions) {
        this.emitter = emitter;
        this.sessionId = sessionId;
        this.completedCallBack = completedCallBack;
        this.questions = questions;
        this.sb = new StringBuilder();
        this.messageType = questions.getMessageType();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(String data) {
        log.info("OpenAI返回数据：{}", data);
        if (messageType == MessageType.IMAGE) {
            subscription.request(1);
            sb.append(data);
            return;
        }
        MessageRes res = MessageRes.builder().message("")
                .end(Boolean.FALSE)
                .messageType(messageType).build();
        MessageRes audioRes = MessageRes.builder().message("")
                .end(Boolean.FALSE)
                .messageType(MessageType.AUDIO).build();
        if ("[DONE]".equals(data)) {
            log.info("OpenAI返回数据结束了");
            subscription.request(1);
            res.setEnd(Boolean.TRUE);
            emitter.next(JSON.toJSONString(R.success(res)));
            completedCallBack.completed(questions, sessionId, sb.toString());
            emitter.complete();
        } else {
            // 檢查數據中是否包含句號或逗號

            if (data.contains("，") || data.contains("。") || data.contains("!") || data.contains("？")) {
                // 轉換累積的文本為語音

                byte[] audioData = azureSpeechServiceimpl.textToSpeech(sentence.toString());
                log.info(audioData.toString());
                String encodedAudio = Base64.getEncoder().encodeToString(audioData);

                audioRes.setMessage(encodedAudio);

                emitter.next(JSON.toJSONString(R.success(audioRes)));
                subscription.request(1);

                // 清空累積的數據
                audioSentnece.setLength(0);
            }


            OpenAiResponse openAiResponse = JSON.parseObject(data, OpenAiResponse.class);
            String content = openAiResponse.getChoices().get(0).getDelta().getContent();
            if( !data.contains("#") ){
                audioSentnece.append(content);
            }
            sentence.append(content);
            log.info(sentence.toString());
            content = content == null ? "" : content;
            res.setMessage(content);
            emitter.next(JSON.toJSONString(R.success(res)));
            sb.append(content);
            subscription.request(1);
        }

    }

    @Override
    public void onError(Throwable t) {
        log.error("OpenAI返回数据异常：{}", t.getMessage());
        if (t.getMessage().contains(OpenAiWebClient.CONTEXT_LENGTH_EXCEEDED)){
            emitter.next(JSON.toJSONString(R.fail("内容超出了限制長度，已经清理歷史紀錄，請重新进行提問")));
            completedCallBack.clearHistory(sessionId);
        }else {
            emitter.next(JSON.toJSONString(R.fail(t.getMessage())));
        }
        emitter.complete();
        completedCallBack.fail(questions, sessionId, t.getMessage());
    }

    @Override
    public void onComplete() {
        log.info("OpenAI返回数据完成");
        if (messageType == MessageType.IMAGE) {
            OpenAiImageResponse aiImageResponse = JSON.parseObject(sb.toString(), OpenAiImageResponse.class);
            String url = aiImageResponse.getData().stream().map(DataRes::getUrl).collect(Collectors.joining(","));
            MessageRes res = MessageRes.builder().message(url).end(true).build();
            emitter.next(JSON.toJSONString(R.success(res)));
        }
        emitter.complete();
    }

    @Override
    public void dispose() {
        log.warn("OpenAI返回数据关闭");
        emitter.complete();
    }
}