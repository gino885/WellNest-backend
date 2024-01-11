package com.wellnest.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public interface OpenAIService {
    String createThread();
    String addMessageToThread(String threadId, String message);
    String runThread(String threadId);
    JSONArray addMessage(String threadId, String message);
    String getRespond(String threadId);
    String getRunStatus(String  threadId, String runId);
    byte[] textToSpeech(String threadId, String inputText);
}
