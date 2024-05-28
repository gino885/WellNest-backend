package com.wellnest.chatbot.controller;

import com.wellnest.chatbot.model.Mission;
import com.wellnest.chatbot.service.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MissionController {

    @Autowired
    private MissionService missionService;

    @GetMapping("/missions/{emotion}")
    public List<Mission> getMissionsByEmotion(@PathVariable String emotion) {
        return missionService.getMissionsByEmotion(emotion);
    }
}