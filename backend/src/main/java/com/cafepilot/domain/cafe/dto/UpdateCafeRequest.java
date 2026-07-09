package com.cafepilot.domain.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCafeRequest(

        @NotBlank(message = "카페 이름은 필수입니다.")
        @Size(max = 100, message = "카페 이름은 100자 이하여야 합니다.")
        String name,

        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        String phone
) {
}
