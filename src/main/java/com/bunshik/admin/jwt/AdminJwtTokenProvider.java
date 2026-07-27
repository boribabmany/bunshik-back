package com.bunshik.admin.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AdminJwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiration;

    public AdminJwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.expiration = expiration;
    }

    // JWT 생성
    public String createToken(Integer adminId, String username) {

        Date now = new Date();
        Date expirationDate =
                new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("adminId", adminId)
                .claim("role", "ADMIN")
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    // JWT 내용 읽기
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // JWT 유효성 검사
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 관리자 아이디 추출
    public Integer getAdminId(String token) {
        return getClaims(token)
                .get("adminId", Integer.class);
    }

    // 관리자 이름 추출
    public String getUsername(String token) {
        return getClaims(token)
                .getSubject();
    }

    // 권한 추출
    public String getRole(String token) {
        return getClaims(token)
                .get("role", String.class);
    }
}