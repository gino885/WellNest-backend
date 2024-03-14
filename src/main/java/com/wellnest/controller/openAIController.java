package com.wellnest.controller;

import com.wellnest.dto.MessageRequest;
import com.wellnest.service.OpenAIService;
import com.wellnest.dto.CreateThreadRequest;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Validated
public class openAIController {

    @Autowired
    private OpenAIService openAIService;


    @PostMapping("/chat/create")
    public ResponseEntity<Map<String, String>> createThread(@RequestBody CreateThreadRequest createThreadRequest) {

        String threadId = openAIService.createThread();
        openAIService.addMessage(threadId, createThreadRequest.CreateMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("threadId", threadId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/chat/message")
    public ResponseEntity<?> addMessage(@RequestBody @Validated MessageRequest messageRequest){
        JSONArray jsonArray = openAIService.addMessage(messageRequest.getThreadId(), messageRequest.getMessage());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(openAIService.getRespond(messageRequest.getThreadId()).toString());
    }
    @GetMapping("/chat/respond/{threadId}")
    public ResponseEntity<String> getRespond(@PathVariable String threadId){
        return ResponseEntity.ok().body(openAIService.getRespond(threadId));
    }
    @PostMapping ("/chat/respond/voice")
    public ResponseEntity<Map<String, String> > getVoice(@RequestBody @Validated MessageRequest messageRequest){
        Map<String, String>  resultMap = openAIService.textToSpeech(messageRequest.getThreadId(), messageRequest.getMessage());

        return ResponseEntity.ok()
                .body(resultMap);
    }



}

