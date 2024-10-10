package com.wellnest.comic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatData {
    private Integer chatId;
    private String title;
    private Map<String, List<?>> urlsByType;
    private Date date;
    private List<Map<String, String>> dialogue;

}
