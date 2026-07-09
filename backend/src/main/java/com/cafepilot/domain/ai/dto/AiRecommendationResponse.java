package com.cafepilot.domain.ai.dto;

import java.util.List;

public record AiRecommendationResponse(
        String summary,
        List<String> recommendations
) {
    public static AiRecommendationResponse of(String rawContent) {
        String[] lines = rawContent.strip().split("\n");
        String summary = lines.length > 0 ? lines[0] : "";
        List<String> recommendations = List.of(lines).subList(
                Math.min(1, lines.length), lines.length
        );
        return new AiRecommendationResponse(summary, recommendations);
    }
}
