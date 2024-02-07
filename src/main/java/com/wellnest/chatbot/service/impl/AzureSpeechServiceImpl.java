package com.wellnest.chatbot.service.impl;

import com.microsoft.cognitiveservices.speech.*;
import com.wellnest.chatbot.service.AzureSpeechService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
@Slf4j
public class AzureSpeechServiceImpl implements AzureSpeechService {
    private static String speechKey = System.getenv("SPEECH_KEY");
    private static String speechRegion = System.getenv("SPEECH_REGION");
    private final ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    @Override
    public byte[] textToSpeech(String text) {
        log.info("1");
        try {
            log.info("1");
            String filePath = getClass().getClassLoader().getResource("ssml.xml").getPath();
            log.info("1");
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            log.info("1");
            speechConfig.setSpeechSynthesisVoiceName("zh-CN-XiaoxiaoNeural");
            log.info("1");
            String ssml = xmlToString(filePath);

            String ssml_text = ssml.replace("{TEXT}", text);

            log.info("1");
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);
            SpeechSynthesisResult result = synthesizer.SpeakSsml(ssml_text);
            AudioDataStream stream = AudioDataStream.fromResult(result);
            log.info("2");
            stream.saveToWavFile("C:\\Users\\USER\\IdeaProjects\\output.wav");
            log.info("3");
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                log.info("语音合成成功");

                synthesizer.close();

                return result.getAudioData();
            } else if (result.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
                System.out.println("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    System.out.println("CANCELED: Did you set the speech resource key and region values?");
                }
                synthesizer.close();
            }
            else {
                log.error("语音合成失败: " + result.getReason());
                synthesizer.close();
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

        }


    private static String xmlToString(String filePath) {
        File file = new File(filePath);
        StringBuilder fileContents = new StringBuilder((int)file.length());

        try (Scanner scanner = new Scanner(file)) {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + System.lineSeparator());
            }
            return fileContents.toString().trim();
        } catch (FileNotFoundException ex) {
            return "File not found.";
        }
    }

}

