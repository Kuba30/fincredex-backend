package com.example.fincredex.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${spring.ai.anthropic.api-key}")
    private String anthropicApiKey;

    @Bean
    public AnthropicChatModel anthropicChatModel() {
        AnthropicApi anthropicApi = AnthropicApi.builder()
                .apiKey(anthropicApiKey)
                .build();

        return AnthropicChatModel.builder()
                .anthropicApi(anthropicApi)
                .defaultOptions(AnthropicChatOptions.builder()
                        .model("claude-sonnet-5")
                        .maxTokens(1000)
//                        .temperature(0.2)
                        .build())
                .build();
    }

    @Bean
    public ChatClient chatClient(AnthropicChatModel anthropicChatModel) {
        return ChatClient.builder(anthropicChatModel).build();
    }
}