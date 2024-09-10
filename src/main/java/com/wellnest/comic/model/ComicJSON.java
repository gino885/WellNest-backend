package com.wellnest.comic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComicJSON {
    @JsonProperty("version")
    private String version;

    @JsonProperty("input")
    private InputParams input;

    @Data
    @Builder
    public static class InputParams {
        @JsonProperty("comic_description")
        private String comicDescription;

        @JsonProperty("character_description")
        private String characterDescription;

        @JsonProperty("sd_model")
        private String sdModel;

        @JsonProperty("num_steps")
        private int numSteps;

        @JsonProperty("style_name")
        private String styleName;

        @JsonProperty("comic_style")
        private String comicStyle;

        @JsonProperty("image_width")
        private int imageWidth;

        @JsonProperty("image_height")
        private int imageHeight;

        @JsonProperty("output_format")
        private String outputFormat;

        @JsonProperty("negative_prompt")
        private String negativePrompt;

        @JsonProperty("style_strength_ratio")
        private Integer style_strengthRatio;

        @JsonProperty("num_ids")
        private Integer numIds;

        @JsonProperty("sa32_setting")
        private Double sa32Setting;

        @JsonProperty("sa64_setting")
        private Double sa64Setting;

        @JsonProperty("guidance_scale")
        private Integer guidanceScale;

        @JsonProperty("output_quality")
        private Integer outputQuality;

    }
}