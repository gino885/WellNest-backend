package com.wellnest.chatbot.service.impl;

import com.wellnest.chatbot.dao.MissionDao;
import com.wellnest.chatbot.model.Mission;
import com.wellnest.chatbot.service.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissionServiceImpl implements MissionService{
    @Autowired
    private MissionDao missionDao;

    public List<Mission> getMissionsByEmotion(String emotion) {
        Mission easyMission = missionDao.findByEmotionByDifficulty(emotion, 0);
        Mission mediumMission = missionDao.findByEmotionByDifficulty(emotion, 1);
        Mission hardMission = missionDao.findByEmotionByDifficulty(emotion, 2);

        return List.of(
                easyMission,
                mediumMission,
                hardMission
        );
    }

}
