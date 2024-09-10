package com.wellnest.chatbot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "mission")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private int missionID;

    @Column(name = "content")
    private String content;

    @Column(name = "difficulty")
    private int difficulty;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "mission_emotion",
            joinColumns = @JoinColumn(name = "mission_id"),
            inverseJoinColumns = @JoinColumn(name = "emotion_id")
    )
    private Set<Emotion> emotions = new HashSet<>();

}
