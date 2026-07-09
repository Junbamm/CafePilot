package com.cafepilot.domain.ai.controller;

import com.cafepilot.domain.ai.dto.AiRecommendationResponse;
import com.cafepilot.domain.ai.service.AiService;
import com.cafepilot.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI", description = "AI 추천 API")
@RestController
@RequestMapping("/api/v1/cafes/{cafeId}/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI 운영 추천 조회",
               description = "현재 메뉴·재고 데이터를 기반으로 AI가 운영 개선 추천을 제공합니다.")
    @GetMapping("/recommendations")
    public ApiResponse<AiRecommendationResponse> getRecommendation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId
    ) {
        return ApiResponse.success(aiService.getRecommendation(memberId, cafeId));
    }
}
