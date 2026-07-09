package com.cafepilot.domain.ai.service;

import com.cafepilot.domain.inventory.entity.Inventory;
import com.cafepilot.domain.menu.entity.Menu;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    private static final String SYSTEM_PROMPT = """
            당신은 카페 운영 전문 컨설턴트입니다.
            카페 오너에게 메뉴, 재고, 운영에 관한 실용적인 조언을 한국어로 제공합니다.
            응답은 첫 줄에 한 줄 요약, 이후 구체적인 추천 사항을 번호 목록으로 작성해주세요.
            """;

    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserPrompt(List<Menu> menus, List<Inventory> inventories) {
        StringBuilder sb = new StringBuilder();
        sb.append("현재 카페 운영 현황입니다. 운영 개선을 위한 추천을 3가지 제공해주세요.\n\n");

        sb.append("## 메뉴 현황\n");
        for (Menu menu : menus) {
            sb.append(String.format("- %s: %s원 (%s) - %s\n",
                    menu.getName(),
                    menu.getPrice().toPlainString(),
                    menu.getCategory().name(),
                    menu.isAvailable() ? "판매중" : "품절"));
        }

        sb.append("\n## 재고 현황\n");
        for (Inventory inv : inventories) {
            String status = inv.isLowStock() ? "⚠️ 부족" : "정상";
            sb.append(String.format("- menuId=%d: 현재 %d개 (임계값 %d개) - %s\n",
                    inv.getMenuId(),
                    inv.getQuantity(),
                    inv.getLowStockThreshold(),
                    status));
        }

        return sb.toString();
    }
}
