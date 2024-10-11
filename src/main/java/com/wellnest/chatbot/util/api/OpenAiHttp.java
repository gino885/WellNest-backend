package com.wellnest.chatbot.util.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Objects;

@Component
public class OpenAiHttp {
    private String description_prompt = "根據以下用戶聊天內容還原他當天一個英文的第二人稱故事情節。Format: 1. Scene description with specific actions " +
            "2. [NC] Scene with no characters, just environmental details 3. Actions of the characters or important events in the story，" +
            "每個情節用 `\n` 分隔，並且只使用英文。請確保生成的情節小於10句，並要提及用戶做的事情如何影響他的心情，聊天內容: {}，事情:{mission}，並且結局要正向。Example Format: " +
            "Discussing project challenges with two students, offering guidance, night\n[NC] An empty classroom, a computer screen flickers, night\n" +
            "Spotting an error in the code on the screen, eyes narrowing, night\n[NC] A whiteboard filled with diagrams, late night\n" +
            "Receiving a student's plea for help, suggesting a different approach on the whiteboard, late night\n" +
            "Sharing an unexpected insight during a lab session, excitement building among students, morning\n" +
            "[NC] The competition venue for an app software contest, crowded with people, bright lights, anticipation building\n" +
            "Delivering a compelling demonstration of the mental health app on the screen, the audience captivated, morning\n" +
            "[NC] Judges announce the app as the winner, applause fills the room, night\n" +
            "The professor smiles, pride shining in his eyes as his students celebrate, night 請按照這個格式生成故事情節，並確保小於10個情節。";
    private String narration_prompt = "Generate a creative and storytelling narration based on the following single-line input of descriptions. Follow these guidelines:\n" +
            "\n" +
            "1. Use Simplified Chinese characters, but with a more colloquial Taiwanese tone and expressions, avoiding overly formal or literary language and ignore time like night or afternoon\n" +
            "2. Each description is separated by \\n in the input.\n" +
            "3. Use [uv_break] for indicating pauses or transitions and [laugh] for indicating laughter where appropriate.\n" +
            "4. Generate character dialogues where appropriate, using [Dialogue_X] tags (X should match the number of the most recent [Narration_X] tag).\n" +
            "5. Each scene's narration starts with [Narration_X] (X is the scene number, which should be consecutive and and match the number of descriptions, up to 10 scenes.).\n" +
            "6. Narrations and dialogues can be interspersed within the same scene.\n" +
            "7. Each narration must be 100 characters or less.\n" +
            "9. For [NC] scenes, focus on atmosphere, setting, and background sounds, even if no characters are present. Each description must generate a corresponding narration regardless of the presence of characters.\n" +
            "10. Not every narration must be followed by dialogue, include dialogues when it is suitable for storytelling.\n" +
            "11. Maintain consistency in timeline and storytelling.\n" +
            "12. Separate each line of narration or dialogue with \\n.\n" +
            "13. Do not use any additional markers or symbols (such as **) in the text.\n" +
            "14. Use second-person perspective for narrations, addressing the reader as 'you'. This creates a more immersive experience.\n" +
            "15. Use first-person perspective for dialogues, but ONLY include the direct speech. All descriptive elements should be in the narration, not the dialogue.\n" +
            "16. Dialogues only contain what the character actually says out loud or thinks directly.\n" +
            "17. Ensure that the scene numbers are consecutive and that dialogue numbers match the most recent narration number.\n" +
            "18. you can naturally revise some of the dialogues or real scenario in the context: {context} to align with the story" +
            "19. Ensure all dialogues are between 10 and 50 characters, which should generate an audio length of at least 2 seconds when processed.\n" +
            "20. Be creative and align with the story's tone.\n" +
            "21. if the description don't need dialogue, the description should only have one narration\n"+
            "22. Each description should have one narration and the narration the scene number X should be exact the , the dialogue depends on the context\n" +
            "23. The number of narrations generated must match the number of descriptions, and the total number of narrations must not exceed 10.\n"+
            "24. output format can only begin with [Narration_X] or [Dialogue_X] \\n"+
            "\n" +
            "Input descriptions: {}\n" +
            "\n" +
            "Output format:\n" +
            "[Narration_X] [Second-person narration] avoid 'they' and 'them'\\n\n" +
            "[Dialogue_X] [First-person character dialogue between 10 and 50 characters to generate at least 2 seconds of audio, but only where necessary for the scene]\\n\n" +
            "[Narration_X] [Continued second-person narration, including any descriptive elements about the dialogue]\\n\n" +
            "...\n" +
            "\n" +
            "Example output:\n" +
            "[Narration_1] 你走进拥挤的教室，感受到紧张的气氛扑面而来。你看到教授正在苦苦想着什么。\n " +
            "[Dialogue_1] 呼——今天真是太难了，每个细节都不顺!\n" +
            "[Narration_1] 教授自言自语道，脸上写满了担忧。你注意到教授的鞋带松了，心里暗自担心他可能会绊倒。\n" +
            "[Narration_2] 突然，教授站起来准备开始讲课，你屏住呼吸，期待着接下来会发生什么。\n" +
            "[Dialogue_2] 哎呀！[uv_break]怎么又是这个问题，烦死了。\n" +
            "[Narration_3] 一个学生突然喊道：[uv_break] 教授被自己的鞋带绊倒了，你忍不住笑出声来[laugh]，同时又为学生感到担心。\n";

    private String caption_prompt = "Based on the following descriptions, generate a corresponding caption for each description，請按照以下規則回覆我：\n" +
            "- 語言：只用繁體中文和台灣慣用語翻譯，勿使用簡體中文和中國慣用語。\n" +
            "- 風格：符合台灣人寫作習慣，通順易讀，並力求文學性及雋永。\n" +
            "- 名詞：用台灣慣用譯法翻譯名詞，同一篇文章內的名詞翻譯需保持一致。\n" +
            "- 格式：所有標點符號必須為全形，中英文之間保持空格，且為5~10個中文字。. The captions should feel more casual and conversational to better match a modern, everyday tone. Ensure that the length of the captions varies for diversity.\n" +
            "\n" +
            "Descriptions are separated by `\\n`. Please return the captions as an array formatted like this:\n" +
            "caption: [caption 1, caption 2, caption 3, ...]\n" +
            "Descriptions:\n" +
            "{}\n" +
            "Format:\n" +
            "caption: [教授在耐心指導學生,教室裡電腦螢幕微微發亮,教授發現錯誤，靜靜思考,白板上寫滿了靈感點子,深夜中，教授給了新方向,早上實驗室裡熱烈討論,比賽場地充滿緊張氛圍,精彩演示，大家屏氣凝神,得獎時全場爆出熱烈掌聲,教授微笑，學生們開心歡呼]\n" +
            "\n" +
            "Please ensure that each caption is creative, accurately reflects the corresponding description, and uses simple and conversational language.\n";
    private String title_prompt = "Based on the following story description, please generate a compelling and concise title that captures the essence and positive ending of the story，請使用繁體中文並在 3 到 8 個字之間: {}";
    public String getChatCompletion(String userMessage,String originalMessage ,String type) throws Exception {
        String urlString = "https://api.openai.com/v1/chat/completions";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + System.getenv("OPENAI_API"));
        connection.setDoOutput(true);
        String safeOriginalMessage = Objects.requireNonNullElse(originalMessage, "");
        String prompt = "";
        switch (type) {
            case "description":
                prompt = description_prompt.replace("{}", userMessage).replace("{mission}", safeOriginalMessage);
                break;
            case "narration":
                prompt = narration_prompt.replace("{}", userMessage).replace("{context}", safeOriginalMessage);
                break;
            case "caption":
                prompt = caption_prompt.replace("{}", userMessage);
                break;
            case "title":
                prompt = title_prompt.replace("{}", userMessage);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("model", "gpt-4o");

        ArrayNode messagesNode = objectMapper.createArrayNode();
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a professional comic writer.");
        messagesNode.add(systemMessage);

        ObjectNode respond = objectMapper.createObjectNode();
        respond.put("role", "user");
        respond.put("content", prompt);
        messagesNode.add(respond);

        rootNode.set("messages", messagesNode);

        String requestBody = objectMapper.writeValueAsString(rootNode);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes());
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            ObjectMapper respondMapper = new ObjectMapper();
            JsonNode jsonNode = respondMapper.readTree(response.toString());

            JsonNode choices = jsonNode.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.path("message");
                return message.path("content").asText();
            } else {
                throw new RuntimeException("No choices found in the response");
            }
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            throw new RuntimeException("Failed : HTTP error code : " + responseCode + " Response: " + response.toString());
        }
    }

}
