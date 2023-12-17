package com.wellnest;

import com.wellnest.dto.MessageRequest;
import com.wellnest.dto.RunThreadRequest;
import com.wellnest.service.OpenAIService;
import com.wellnest.dto.UserRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<String> createThread(@RequestBody UserRequest userRequest) {

        String threadId = openAIService.createThread();
        openAIService.addMessage(threadId,userRequest.CreateMessage());
        return ResponseEntity.ok().body(threadId);
    }

    @PostMapping("/chat/run")
    public ResponseEntity<?> runThread(@RequestBody RunThreadRequest runThreadRequest) {
            openAIService.runThread(runThreadRequest.getThreadId());
            return ResponseEntity.ok().body("ok");
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



}

