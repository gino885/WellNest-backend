package com.wellnest.chatbot.model;
import com.wellnest.comic.model.Comic;
import com.wellnest.user.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "chat")
@Data
@Builder
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Integer chatId;

    @Column(name = "status")
    private String status;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "title")
    private String title;

    @Column(name = "mission")
    private String mission;

    @Column(name = "emotion")
    private List<String> emotion;

    @Column(name = "share")
    private Boolean share;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}