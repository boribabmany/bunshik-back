package com.bunshik.common.config;

import com.bunshik.admin.jwt.AdminJwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // 기존 CorsConfig 사용
                .cors(cors -> {
                })

                // JWT 방식이므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 서버 세션을 사용하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // 관리자 로그인은 토큰 없이 허용
                        .requestMatchers("/api/admin/login").permitAll()

                        // 메뉴·옵션 이미지 조회 허용
                        .requestMatchers("/images/**").permitAll()

                        // 관리자 API는 JWT 인증 필요
                        .requestMatchers("/api/admin/**").authenticated()

                        // 키오스크를 포함한 나머지 API는 그대로 허용
                        .requestMatchers("/api/**").permitAll()

                        // 그 외 요청 허용
                        .anyRequest().permitAll()
                )

                // Spring 기본 로그인 방식 사용하지 않음
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())

                // 요청 전에 JWT 필터 실행
                .addFilterBefore(
                        adminJwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}