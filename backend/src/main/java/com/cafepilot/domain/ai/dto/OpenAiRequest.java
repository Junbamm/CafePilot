package com.cafepilot.domain.ai.dto;

import java.util.List;

public record OpenAiRequest(
        String model,
        List<Message> messages,
        int maxTokens
) {
    public record Message(String role, String content) {
        public static Message system(String content) {
            return new Message("system", content);
        }

        public static Message user(String content) {
            return new Message("user", content);
        }
    }

    public static OpenAiRequest of(String model, String systemPrompt, String userPrompt) {
        return new OpenAiRequest(
                model,
                List.of(Message.system(systemPrompt), Message.user(userPrompt)),
                1000
        );
    }
}
