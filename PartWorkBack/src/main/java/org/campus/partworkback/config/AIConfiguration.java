package org.campus.partworkback.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfiguration {
    @Bean
    public ChatClient chatClient(OpenAiChatModel model) {
        return ChatClient
                .builder(model)
                .defaultSystem("现在给你一个任务列表，和一些提示词，请根据提示词选出匹配的任务，你给出的格式为任务的id，id之间使用'，'分隔，若没有找到匹配的任务，则直接返回-1，请严格按照这个格式生成回答")
                .build();
    }
}
