package com.wellnest.comic.model;

import com.wellnest.chatbot.model.Chat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "collection")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "url")
    private String url;

    @Column(name = "type")
    private String type;

    @Column(name = "page")
    private Integer page;

    @Column(name = "date")
    private Date date;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "chat_id", insertable = false, updatable = false)
    private Integer chatId;

}
