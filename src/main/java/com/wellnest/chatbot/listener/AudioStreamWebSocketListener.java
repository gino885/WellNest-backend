package com.wellnest.chatbot.listener;

import com.microsoft.cognitiveservices.speech.AudioDataStream;
import com.wellnest.chatbot.service.AzureSpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class AudioStreamWebSocketListener extends TextWebSocketHandler {
    @Autowired
    private  AzureSpeechService azureSpeechService;

    public AudioStreamWebSocketListener(AzureSpeechService azureSpeechService) {
        this.azureSpeechService = azureSpeechService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String text = message.getPayload();
        AudioDataStream audioDataStream = azureSpeechService.textToSpeech(text);

        if (audioDataStream != null) {
            byte[] buffer = new byte[16000];
            long filledSize = audioDataStream.readData(buffer);
            while (filledSize > 0) {
                session.sendMessage(new BinaryMessage(buffer, 0, (int) filledSize, true));
                filledSize = audioDataStream.readData(buffer);
            }
        }
    }
}