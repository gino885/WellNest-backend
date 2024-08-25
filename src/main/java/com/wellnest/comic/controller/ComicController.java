package com.wellnest.comic.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wellnest.comic.model.ComicRequest;
import com.wellnest.comic.service.ChatTTSService;
import com.wellnest.comic.service.ComicService;
import com.wellnest.comic.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/comic")
@Slf4j
@EnableAsync
public class ComicController {

    @Autowired
    private ComicService comicService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private ChatTTSService chatTTSService;

    private static final Logger logger = LoggerFactory.getLogger(ComicController.class);

    @PostMapping
    public ResponseEntity<?> generateComic(@RequestBody ComicRequest comicRequest) throws Exception {
        try {
            List<String> imageUrls = comicService.generateComic(comicRequest);

            List<String> imagePaths;
            try {
                imagePaths = imageService.mergeImages(imageUrls, comicRequest.getCaption());
                log.info("Images merged successfully.");
            } catch (Exception e) {
                log.error("Error during image merging: {}", e.getMessage());
                throw e;
            }

            try {
                chatTTSService.generateNarration(comicRequest.getNarration());
                log.info("Narration generated successfully.");
            } catch (Exception e) {
                log.error("Error during narration generation: {}", e.getMessage());
                throw e;
            }

            log.info("Images and narration processing completed.");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity.ok().headers(headers).build();

        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON format: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}


