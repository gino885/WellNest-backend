package com.wellnest.comic.model;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class ChatData {
    private String title;
    private Map<String, List<String>> urlsByType;
    private Date date;

    public ChatData(String title, Map<String, List<String>> urlsByType, Date date) {
        this.title = title;
        this.urlsByType = urlsByType;
        this.date = date;
    }

}
