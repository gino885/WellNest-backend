package com.wellnest.chatbot.service;

import com.microsoft.cognitiveservices.speech.AudioDataStream;

public interface AzureSpeechService {
    byte[] textToSpeech(String text);
    void setText (String text);
    public boolean isNull();
    boolean checkStatus();
    void setDone();
}
