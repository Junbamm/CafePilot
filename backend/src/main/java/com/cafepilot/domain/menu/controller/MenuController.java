package com.cafepilot.domain.menu.controller;

import com.cafepilot.domain.menu.dto.CreateMenuRequest;
import com.cafepilot.domain.menu.dto.MenuResponse;
import com.cafepilot.domain.menu.dto.UpdateMenuRequest;
import com.cafepilot.domain.menu.service.MenuService;
import com.cafepilot.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Menu", description = "메뉴 API")
@RestController
@RequestMapping("/api/v1/cafes/{cafeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "메뉴 목록 조회")
    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenus(@PathVariable Long cafeId) {
        return ApiResponse.success(menuService.getMenus(cafeId));
    }

    @Operation(summary = "메뉴 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MenuResponse> createMenu(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId,
            @Valid @RequestBody CreateMenuRequest request
    ) {
        return ApiResponse.success(menuService.createMenu(memberId, cafeId, request));
    }

    @Operation(summary = "메뉴 수정")
    @PutMapping("/{menuId}")
    public ApiResponse<MenuResponse> updateMenu(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId,
            @PathVariable Long menuId,
            @Valid @RequestBody UpdateMenuRequest request
    ) {
        return ApiResponse.success(menuService.updateMenu(memberId, cafeId, menuId, request));
    }

    @Operation(summary = "메뉴 판매 상태 토글 (판매중/품절)")
    @PatchMapping("/{menuId}/available")
    public ApiResponse<MenuResponse> toggleAvailable(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId,
            @PathVariable Long menuId
    ) {
        return ApiResponse.success(menuService.toggleAvailable(memberId, cafeId, menuId));
    }

    @Operation(summary = "메뉴 삭제")
    @DeleteMapping("/{menuId}")
    public ApiResponse<Void> deleteMenu(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId,
            @PathVariable Long menuId
    ) {
        menuService.deleteMenu(memberId, cafeId, menuId);
        return ApiResponse.success();
    }
}
