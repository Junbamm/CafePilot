package com.cafepilot.domain.auth.service;

import com.cafepilot.domain.auth.dto.LoginRequest;
import com.cafepilot.domain.auth.dto.RegisterRequest;
import com.cafepilot.domain.auth.dto.TokenResponse;
import com.cafepilot.domain.auth.exception.AuthException;
import com.cafepilot.domain.member.entity.Member;
import com.cafepilot.domain.member.repository.MemberRepository;
import com.cafepilot.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("AuthService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;

    @InjectMocks AuthService authService;

    @Test
    @DisplayName("이미 존재하는 이메일로 가입하면 예외가 발생한다")
    void register_duplicateEmail() {
        RegisterRequest request = new RegisterRequest("dup@test.com", "password1!", "홍길동");
        given(memberRepository.existsByEmail("dup@test.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("신규 이메일로 회원가입이 성공한다")
    void register_success() {
        RegisterRequest request = new RegisterRequest("new@test.com", "password1!", "홍길동");
        given(memberRepository.existsByEmail("new@test.com")).willReturn(false);
        given(passwordEncoder.encode("password1!")).willReturn("encodedPw");

        authService.register(request);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
    void login_memberNotFound() {
        LoginRequest request = new LoginRequest("notfound@test.com", "password");
        given(memberRepository.findByEmailAndDeletedAtIsNull("notfound@test.com"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 예외가 발생한다")
    void login_wrongPassword() {
        Member member = Member.createOwner("test@test.com", "encodedPw", "홍길동");
        LoginRequest request = new LoginRequest("test@test.com", "wrongPw");

        given(memberRepository.findByEmailAndDeletedAtIsNull("test@test.com"))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrongPw", "encodedPw")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("올바른 정보로 로그인하면 토큰이 반환된다")
    void login_success() {
        Member member = Member.createOwner("test@test.com", "encodedPw", "홍길동");
        LoginRequest request = new LoginRequest("test@test.com", "rawPw");

        given(memberRepository.findByEmailAndDeletedAtIsNull("test@test.com"))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches("rawPw", "encodedPw")).willReturn(true);
        given(jwtProvider.generateAccessToken(member)).willReturn("accessToken");
        given(jwtProvider.generateRefreshToken(member)).willReturn("refreshToken");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        TokenResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
