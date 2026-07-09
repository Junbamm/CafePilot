package com.cafepilot.domain.member.service;

import com.cafepilot.domain.member.dto.MemberResponse;
import com.cafepilot.domain.member.entity.Member;
import com.cafepilot.domain.member.exception.MemberException;
import com.cafepilot.domain.member.repository.MemberRepository;
import com.cafepilot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberResponse getMyInfo(Long memberId) {
        Member member = findActiveMember(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateName(Long memberId, String name) {
        Member member = findActiveMember(memberId);
        member.changeName(name);
        return MemberResponse.from(member);
    }

    @Transactional
    public void changePassword(Long memberId, String currentPassword, String newPassword) {
        Member member = findActiveMember(memberId);

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new MemberException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        member.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = findActiveMember(memberId);
        member.softDelete();
    }

    private Member findActiveMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            throw new MemberException(ErrorCode.MEMBER_INACTIVE);
        }

        return member;
    }
}
