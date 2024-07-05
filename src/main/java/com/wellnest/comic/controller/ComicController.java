package com.wellnest.comic.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wellnest.comic.model.ComicJSON;
import com.wellnest.comic.model.ComicRequest;
import com.wellnest.comic.service.ComicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comic")
public class ComicController {

        @Autowired
        private ComicService comicService;

        @GetMapping
        public ResponseEntity<?> generateComic(@RequestParam ComicRequest comicRequest) throws JsonProcessingException, InterruptedException{
                String predictionId = comicService.generateComic(comicRequest);
                if (predictionId != null) {
                    String output = comicService.pollPredictionStatus(predictionId);
                    return ResponseEntity.status(HttpStatus.CREATED).body(output);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }

        }
    }

