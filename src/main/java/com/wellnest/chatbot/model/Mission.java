package com.wellnest.chatbot.model;

public class Mission {
    private int missionID;
    private String emotion;
    private String content;
    private int difficulty;

    public int getMissionID() {
        return missionID;
    }

    public void setMissionID(int missionID) {
        this.missionID = missionID;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}

