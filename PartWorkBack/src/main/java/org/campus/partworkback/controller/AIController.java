package org.campus.partworkback.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.campus.partworkback.DTO.MatchDTO;
import org.campus.partworkback.Service.AIService;
import org.campus.partworkback.pojo.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    @Autowired
    private AIService aiService;

    private final ChatClient chatClient;

//    @GetMapping(value = "/match", produces = "text/html;charset=utf-8")
//    public Flux<String> chat(String prompt){
//        return chatClient.prompt()
//                .user(prompt)
//                .stream()
//                .content();
//    }
    @GetMapping("/match")
    public Result matchTask(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("currentUserId");
        List<MatchDTO> list = aiService.matchTask(userId);
        return Result.success(list);
    }
}
