package com.wellnest.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public interface OpenAIService {
    String createAssistant(String name, String instructions, String model) throws IOException;
    String createThread();
    String addMessageToThread(String threadId, String message);
    String runThread(String threadId);
    JSONArray addMessage(String threadId, String message);
    String getRespond(String threadId);
    String getRunStatus(String  threadId, String runId);
}
