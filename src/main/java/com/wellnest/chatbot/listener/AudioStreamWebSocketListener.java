package com.wellnest.chatbot.listener;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
@Slf4j
public class AudioStreamWebSocketListener extends TextWebSocketHandler {

        private static String speechKey = System.getenv("SPEECH_KEY");
        private static String speechRegion = System.getenv("SPEECH_REGION");
        private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    @PostConstruct
    public void init() {
        log.info("AudioStreamWebSocketListener initialized");
    }


    @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, ExecutionException, IOException {

            String filePath = getClass().getClassLoader().getResource("ssml.xml").getPath();
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechSynthesisVoiceName("zh-CN-XiaoxiaoNeural");
            String ssml = xmlToString(filePath);
            String ssml_text = ssml.replace("{TEXT}", message.getPayload());

            SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);
            SpeechSynthesisResult result = synthesizer.SpeakSsml(ssml_text);
            AudioDataStream audioDataStream = AudioDataStream.fromResult(result);
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                log.info("语音合成成功");
            } else {
                log.error("语音合成失败: " + result.getReason());
            }
            while (true) {

            byte[] buffer = new byte[16000];
            long filledSize = audioDataStream.readData(buffer);
                if (filledSize <= 0) {
                    // 没有更多数据可读，结束循环
                    log.info("failing to Send audio data of size: " + filledSize);
                    break;
                }
                if (filledSize < buffer.length) {
                    // 如果读取的数据没有填满 buffer，创建一个新的更小的 buffer 来发送
                    byte[] actualData = Arrays.copyOf(buffer, (int)filledSize);
                    session.sendMessage(new BinaryMessage(actualData));
                    log.info("Sending audio data of size: " + filledSize);
                } else {
                    // 如果 buffer 完全填满，直接发送
                    session.sendMessage(new BinaryMessage(buffer));
                    log.info("Sending audio data of size: " + filledSize);
                }
            }
            synthesizer.close();
        }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established with session ID: " + session.getId());
        URI uri = session.getUri();
        if (uri != null) {
            String userId = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("userId");
            log.info("WebSocket connection established with User ID: " + userId);
            if (userId != null) {
                sessions.put(userId, session);
                System.out.println(sessions);
            } else {
                log.warn("User ID not found in query string");
            }
        } else {
            log.warn("Session URI is null");
        }
        System.out.println(sessions);


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        log.info("WebSocket connection closed with session ID: " + session.getId());
    }



    public WebSocketSession getSession(String userId){
        WebSocketSession session = sessions.get(userId);
        System.out.println(sessions);
        if (session == null) {
            log.warn("No session found for User ID: " + userId);
        }
        return session;

    }
    private static String xmlToString(String filePath) {
        File file = new File(filePath);
        StringBuilder fileContents = new StringBuilder((int)file.length());

        try (Scanner scanner = new Scanner(file)) {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + System.lineSeparator());
            }
            return fileContents.toString().trim();
        } catch (FileNotFoundException ex) {
            return "File not found.";
        }
    }

    }
