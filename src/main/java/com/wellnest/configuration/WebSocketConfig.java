package com.wellnest.configuration;

import com.wellnest.chatbot.listener.AudioStreamWebSocketListener;
import com.wellnest.chatbot.service.AzureSpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new AudioStreamWebSocketListener(), "/audioStream").setAllowedOrigins("*");
    }
}
