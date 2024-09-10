package com.wellnest.comic.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComicRequest {
    private String comicDescription;
    private String characterDescription;
    private String narration;
    private String[] caption;
    private String styleName;
}
