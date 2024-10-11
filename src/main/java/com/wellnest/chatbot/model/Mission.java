package com.wellnest.chatbot.model;

import com.wellnest.comic.model.ChatData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
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

    @Transient
    private ChatData chatData;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "mission_emotion",
            joinColumns = @JoinColumn(name = "mission_id"),
            inverseJoinColumns = @JoinColumn(name = "emotion_id")
    )
    private Set<Emotion> emotions = new HashSet<>();
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mission mission = (Mission) o;
        return missionID == mission.missionID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(missionID);
    }

}
