package com.wellnest.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public interface OpenAIService {
    String createThread();
    String addMessageToThread(String threadId, String message);
    String runThread(String threadId);
    JSONArray addMessage(String threadId, String message);
    String getRespond(String threadId);
    String getRunStatus(String  threadId, String runId);
    Map<String, String> textToSpeech(String threadId, String inputText);
    Map<String, String> playHt(String threadId, String inputText);
}
