package com.wellnest.chatbot.controller;

import com.alibaba.fastjson.JSON;
import com.wellnest.chatbot.enmus.MessageType;
import com.wellnest.chatbot.exception.CommonException;
import com.wellnest.chatbot.service.AzureSpeechService;
import com.wellnest.chatbot.service.UserChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.wellnest.chatbot.service.dto.Message;
import com.wellnest.chatbot.util.R;
import com.wellnest.chatbot.util.api.OpenAiWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author niuxiangqian
 * @version 1.0
 * @date 2023/3/21 16:18
 **/
@Slf4j
@RestController
@RequestMapping({"/chat"})
@RequiredArgsConstructor
public class OpenAiController {

    private final UserChatService userChatService;
    private final OpenAiWebClient openAiWebClient;

    private static final String ERROR_MSG = "使用的人太多啦！等下再用吧！";
    /**
     * 建议更换为自己业务的线程池
     */
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(10);
    private static final Random RANDOM = new Random();
    private StringBuilder textBuffer = new StringBuilder();


    /**
     * 发信息
     *
     * @param prompt 提示词
     * @param user   用户
     * @return
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamCompletions(String prompt, String user) {
        Assert.hasLength(user, "user不能为空");
        Assert.hasLength(prompt, "prompt不能为空");
        Flux<String> respond = userChatService.send(MessageType.TEXT, prompt, user);

        try {
            return respond;
        } catch (CommonException e) {
            log.warn("e:{}", e.getMessage());
            e.printStackTrace();
            return getErrorRes(e.getMessage());
        } catch (Exception e) {
            log.error("e:{}", e.getMessage(), e);
            e.printStackTrace();
            return getErrorRes(ERROR_MSG);
        }
    }



    /**
     * post方式，可以解决特殊符号，过长的文本等问题
     *
     * @return
     */
    @PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamCompletionsPost(@RequestBody Map<String, String> param) {
        String user = param.get("user");
        String prompt = param.get("prompt");

        Assert.hasLength(user, "user不能为空");
        Assert.hasLength(prompt, "prompt不能为空");
        try {
            return userChatService.send(MessageType.TEXT, prompt, user);
        } catch (CommonException e) {
            log.warn("e:{}", e.getMessage());
            e.printStackTrace();
            return getErrorRes(e.getMessage());
        } catch (Exception e) {
            log.error("e:{}", e.getMessage(), e);
            e.printStackTrace();
            return getErrorRes(ERROR_MSG);
        }
    }


    /**
     * 内容检测
     *
     * @param content
     * @return
     */
    @GetMapping("/checkContent")
    public Mono<ServerResponse> checkContent(@RequestParam String content) {
        log.info("req:{}", content);
        return openAiWebClient.checkContent(content);
    }

    /**
     * 获取历史记录
     *
     * @param user
     */
    @GetMapping(value = "/history")
    public Mono<List<Message>> history(String user) {
        Assert.hasLength(user, "user不能为空");
        return Mono.just(userChatService.getHistory(user));
    }

    /**
     * @param msg
     * @return
     */
    private Flux<String> getErrorRes(String msg) {
        return Flux.create(emitter -> {
            emitter.next(" ");
            emitter.next(" ");
            EXECUTOR.execute(() -> {
                try {
                    int time = RANDOM.nextInt(200);
                    // 请注意！这里加线程池休眠是为了解决一个问题，如果你不需要则删除掉这里线程池就行
                    // 问题：假如系统使用了nginx负载均衡，然后后端这个接口遇到异常立即断开sse会导致nginx重连，进而重复请求后端
                    // 所以休眠一下再断开让nginx知道正常连接了，不要重连

                    //不延迟的话nginx会重连sse，导致nginx重复请求后端
                    Thread.sleep(Math.max(time, 100));
                } catch (InterruptedException e) {
                    log.info("e:", e);
                }
                emitter.next(JSON.toJSONString(R.fail(msg)));
                emitter.complete();
            });
        });
    }

}
