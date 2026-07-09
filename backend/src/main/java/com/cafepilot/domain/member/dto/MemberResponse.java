package com.cafepilot.domain.member.dto;

import com.cafepilot.domain.member.entity.Member;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String name,
        String role,
        LocalDateTime createdAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole().name(),
                member.getCreatedAt()
        );
    }
}
