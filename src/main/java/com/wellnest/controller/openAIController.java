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

@RestController
@Validated
public class openAIController {

    @Autowired
    private OpenAIService openAIService;


    @PostMapping("/chat/create")
    public ResponseEntity<String> createThread(@RequestBody CreateThreadRequest createThreadRequest) {

        String threadId = openAIService.createThread();
        openAIService.addMessage(threadId, createThreadRequest.CreateMessage());
        return ResponseEntity.ok().body(threadId);
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
    public ResponseEntity<byte[]> getVoice(@RequestBody String text){
        byte[] audioBytes = openAIService.textToVoice(text);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        return ResponseEntity.ok()
                .headers(headers)
                .body(audioBytes);
    }



}

