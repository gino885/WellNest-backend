package com.wellnest.service;

import org.json.JSONObject;

import javax.print.attribute.standard.JobSheets;
import java.io.IOException;

public interface PlayHtService {
    String getAudio(String text);
    String getAudioUrl(String transcribeId);
}
