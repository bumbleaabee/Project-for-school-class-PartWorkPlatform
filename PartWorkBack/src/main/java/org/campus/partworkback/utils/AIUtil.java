package org.campus.partworkback.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AIUtil {
    private final ChatClient chatClient;

    public String chat(String prompt){
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
