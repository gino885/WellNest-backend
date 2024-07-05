package com.wellnest.comic.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComicRequest {
    private String comicDescription;
    private String refImageUrl;
    private String characterDescription;
}
