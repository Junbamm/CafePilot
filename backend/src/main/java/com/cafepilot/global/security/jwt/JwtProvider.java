package com.cafepilot.global.security.jwt;

import com.cafepilot.domain.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_EMAIL = "email";

    private final SecretKey secretKey;
    private final long accessTokenExpireMs;
    private final long refreshTokenExpireMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expire-ms}") long accessTokenExpireMs,
            @Value("${jwt.refresh-token-expire-ms}") long refreshTokenExpireMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireMs = accessTokenExpireMs;
        this.refreshTokenExpireMs = refreshTokenExpireMs;
    }

    @NonNull
    public String generateAccessToken(@NonNull Member member) {
        return buildToken(member, accessTokenExpireMs);
    }

    @NonNull
    public String generateRefreshToken(@NonNull Member member) {
        return buildToken(member, refreshTokenExpireMs);
    }

    @NonNull
    private String buildToken(@NonNull Member member, long expireMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim(CLAIM_EMAIL, member.getEmail())
                .claim(CLAIM_ROLE, member.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMs))
                .signWith(secretKey)
                .compact();
    }

    @NonNull
    public Claims parseClaims(@NonNull String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(@NonNull String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getMemberId(@NonNull String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    @NonNull
    public String getRole(@NonNull String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }
}
