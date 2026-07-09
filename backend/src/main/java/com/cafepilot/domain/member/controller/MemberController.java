package com.cafepilot.domain.member.controller;

import com.cafepilot.domain.member.dto.ChangePasswordRequest;
import com.cafepilot.domain.member.dto.MemberResponse;
import com.cafepilot.domain.member.dto.UpdateNameRequest;
import com.cafepilot.domain.member.service.MemberService;
import com.cafepilot.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.success(memberService.getMyInfo(memberId));
    }

    @Operation(summary = "이름 수정")
    @PatchMapping("/me/name")
    public ApiResponse<MemberResponse> updateName(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody UpdateNameRequest request
    ) {
        return ApiResponse.success(memberService.updateName(memberId, request.name()));
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        memberService.changePassword(memberId, request.currentPassword(), request.newPassword());
        return ApiResponse.success();
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal Long memberId) {
        memberService.withdraw(memberId);
        return ApiResponse.success();
    }
}
