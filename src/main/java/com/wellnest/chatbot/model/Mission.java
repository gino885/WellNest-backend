package com.wellnest.chatbot.model;

import jakarta.persistence.*;

@Entity // 標記為JPA實體
@Table(name = "mission") // 定義對應的表名為mission
public class Mission {

    @Id // 标记为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 标识符生成策略为数据库自增
    private int missionID;

    @Column(nullable = false)
    private String emotion;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
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
