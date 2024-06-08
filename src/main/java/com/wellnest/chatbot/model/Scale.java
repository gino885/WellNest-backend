package com.wellnest.chatbot.model;

import com.wellnest.user.model.User;
import jakarta.persistence.*;

import java.util.Date;
@Entity
@Table(name = "scale")
public class Scale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scale_id")
    private int chatId;

    @Column(name = "score")
    private int score;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}

