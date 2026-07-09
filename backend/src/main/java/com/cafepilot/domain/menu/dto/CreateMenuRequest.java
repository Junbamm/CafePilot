package com.cafepilot.domain.menu.dto;

import com.cafepilot.domain.menu.entity.Menu;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateMenuRequest(

        @NotBlank(message = "메뉴 이름은 필수입니다.")
        @Size(max = 100, message = "메뉴 이름은 100자 이하여야 합니다.")
        String name,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
        String description,

        @NotNull(message = "가격은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.")
        BigDecimal price,

        @NotNull(message = "카테고리는 필수입니다.")
        Menu.Category category,

        @Min(value = 0, message = "노출 순서는 0 이상이어야 합니다.")
        int displayOrder
) {
}
