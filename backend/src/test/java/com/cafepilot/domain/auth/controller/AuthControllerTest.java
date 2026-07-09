package com.cafepilot.domain.auth.controller;

import com.cafepilot.domain.auth.dto.LoginRequest;
import com.cafepilot.domain.auth.dto.RegisterRequest;
import com.cafepilot.domain.auth.dto.TokenResponse;
import com.cafepilot.domain.auth.exception.AuthException;
import com.cafepilot.domain.auth.service.AuthService;
import com.cafepilot.global.config.SecurityConfig;
import com.cafepilot.global.exception.ErrorCode;
import com.cafepilot.global.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController 슬라이스 테스트")
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("올바른 요청으로 회원가입하면 201을 반환한다")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "홍길동");
        willDoNothing().given(authService).register(any());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이메일 형식이 잘못되면 400을 반환한다")
    void register_invalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "password123", "홍길동");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("중복 이메일로 가입하면 409를 반환한다")
    void register_duplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("dup@example.com", "password123", "홍길동");
        doThrow(new AuthException(ErrorCode.AUTH_EMAIL_DUPLICATED))
                .when(authService).register(any());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_DUPLICATED"));
    }

    @Test
    @DisplayName("올바른 정보로 로그인하면 토큰을 반환한다")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        TokenResponse tokenResponse = TokenResponse.of("accessToken", "refreshToken");
        given(authService.login(any())).willReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 401을 반환한다")
    void login_wrongPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        given(authService.login(any()))
                .willThrow(new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @WithMockUser
    @DisplayName("인증된 사용자는 로그아웃에 성공한다")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
