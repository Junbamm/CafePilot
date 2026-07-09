package com.cafepilot.domain.ai.dto;

import java.util.List;

public record OpenAiApiResponse(
        List<Choice> choices
) {
    public record Choice(Message message) {}

    public record Message(String role, String content) {}

    public String extractContent() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        return choices.get(0).message().content();
    }
}
