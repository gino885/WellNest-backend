package com.wellnest.comic.model;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ChatData {
    private String title;
    private Map<String, List<?>> urlsByType;
    private Date date;
    private List<Map<String, String>> dialogue;

    public ChatData(String title, Map<String, List<?>> urlsByType, Date date, List<Map<String, String>> dialogue) {
        this.title = title;
        this.urlsByType = urlsByType;
        this.date = date;
        this.dialogue = dialogue;
    }

}
