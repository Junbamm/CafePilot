package com.cafepilot.domain.auth.service;

import com.cafepilot.domain.auth.dto.LoginRequest;
import com.cafepilot.domain.auth.dto.RegisterRequest;
import com.cafepilot.domain.auth.dto.TokenResponse;
import com.cafepilot.domain.auth.exception.AuthException;
import com.cafepilot.domain.member.entity.Member;
import com.cafepilot.domain.member.repository.MemberRepository;
import com.cafepilot.global.exception.ErrorCode;
import com.cafepilot.global.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final long refreshTokenExpireMs;

    public AuthService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            StringRedisTemplate redisTemplate,
            @Value("${jwt.refresh-token-expire-ms}") long refreshTokenExpireMs
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpireMs = refreshTokenExpireMs;
    }

    public void register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new AuthException(ErrorCode.AUTH_EMAIL_DUPLICATED);
        }

        Member member = Member.createOwner(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );
        memberRepository.save(member);
    }

    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(member);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + member.getId(),
                refreshToken,
                Duration.ofMillis(refreshTokenExpireMs)
        );

        return TokenResponse.of(accessToken, refreshToken);
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        Long memberId = jwtProvider.getMemberId(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + memberId);

        if (!refreshToken.equals(storedToken)) {
            throw new AuthException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(ErrorCode.AUTH_TOKEN_INVALID));

        String newAccessToken = jwtProvider.generateAccessToken(member);
        String newRefreshToken = jwtProvider.generateRefreshToken(member);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + memberId,
                newRefreshToken,
                Duration.ofMillis(refreshTokenExpireMs)
        );

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    public void logout(Long memberId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + memberId);
    }
}
