package com.wellnest.chatbot.service;

import com.microsoft.cognitiveservices.speech.AudioDataStream;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface AzureSpeechService {
    byte[] textToSpeech(String text);

}
