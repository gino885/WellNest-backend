package com.wellnest.chatbot.service.impl;

import com.microsoft.cognitiveservices.speech.*;
import com.wellnest.chatbot.service.AzureSpeechService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
@Component
public class AzureSpeechServiceImpl implements AzureSpeechService {
    private static String speechKey = System.getenv("SPEECH_KEY");
    private static String speechRegion = System.getenv("SPEECH_REGION");
    private final ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    @Override
    public AudioDataStream textToSpeech(String text) {

        try {

            String ssml = "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xmlns:mstts='https://www.w3.org/2001/mstts' xml:lang='zh-CN'>" +
                    "<voice name='my-custom-voice'>" +
                    "<mstts:express-as style='chat' styledegree='2'>" +
                    text +
                    "</mstts:express-as>" +
                    "</voice></speak>";
            SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechSynthesisVoiceName("zh-CN-XiaoxiaoNeural");

            SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(speechConfig, null);
            SpeechSynthesisResult speechSynthesisResult  =speechSynthesizer.SpeakSsmlAsync(ssml).get();
            if (text.isEmpty()) {
                return null;
            }

            if (speechSynthesisResult.getReason() == ResultReason.SynthesizingAudioCompleted) {
                    return AudioDataStream.fromResult(speechSynthesisResult);

            } else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
                System.out.println("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    System.out.println("CANCELED: Did you set the speech resource key and region values?");
                }
            }

            System.exit(0);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

        }
    }

