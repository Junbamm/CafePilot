package com.cafepilot.domain.ai.service;

import com.cafepilot.domain.ai.dto.AiRecommendationResponse;
import com.cafepilot.domain.ai.dto.OpenAiApiResponse;
import com.cafepilot.domain.ai.dto.OpenAiRequest;
import com.cafepilot.domain.cafe.entity.Cafe;
import com.cafepilot.domain.cafe.exception.CafeException;
import com.cafepilot.domain.cafe.repository.CafeRepository;
import com.cafepilot.domain.inventory.entity.Inventory;
import com.cafepilot.domain.inventory.repository.InventoryRepository;
import com.cafepilot.domain.menu.entity.Menu;
import com.cafepilot.domain.menu.repository.MenuRepository;
import com.cafepilot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiService {

    private final CafeRepository cafeRepository;
    private final MenuRepository menuRepository;
    private final InventoryRepository inventoryRepository;
    private final PromptBuilder promptBuilder;
    private final RestClient openAiRestClient;

    @Value("${ai.openai.model}")
    private String model;

    public AiRecommendationResponse getRecommendation(Long memberId, Long cafeId) {
        Cafe cafe = cafeRepository.findByIdAndDeletedAtIsNull(cafeId)
                .orElseThrow(() -> new CafeException(ErrorCode.CAFE_NOT_FOUND));

        if (!cafe.isOwnedBy(memberId)) {
            throw new CafeException(ErrorCode.CAFE_ACCESS_DENIED);
        }

        List<Menu> menus = menuRepository
                .findByCafeIdAndDeletedAtIsNullOrderByDisplayOrderAsc(cafeId);
        List<Inventory> inventories = inventoryRepository.findByCafeId(cafeId);

        String userPrompt = promptBuilder.buildUserPrompt(menus, inventories);

        OpenAiRequest request = OpenAiRequest.of(
                model,
                promptBuilder.getSystemPrompt(),
                userPrompt
        );

        log.debug("[AiService] OpenAI 호출 cafeId={}", cafeId);

        OpenAiApiResponse apiResponse = openAiRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(OpenAiApiResponse.class);

        String content = apiResponse != null ? apiResponse.extractContent() : "추천 정보를 가져올 수 없습니다.";
        return AiRecommendationResponse.of(content);
    }
}
