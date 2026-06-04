package com.jeonhong.book.springboot.config.auth;

import com.jeonhong.book.springboot.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //H2 Console 사용 설정
                .csrf(csrf -> csrf.disable())
                .headers(headers ->
                        headers.frameOptions(frameOption -> frameOption.disable())
                )
                // URL 별 권한 관리
                .authorizeHttpRequests(auth -> auth
                        // 정적리소스와 메인페이지 누구나 접근 가능
                        .requestMatchers(
                                "/",
                                "/css/**",
                                "/images/**",
                                "/js/**",
                                "/h2-console/**",
                                "/profile",
                                "/login/demo"
                        ).permitAll()

                        // API는 USER 권한을 가진 사용자만 접근 가능
                        .requestMatchers("/api/v1/**")
                        .hasRole(Role.USER.name())

                        // 나머지 요청은 인증된 사용자만 접근 가능
                        .anyRequest()
                        .authenticated()
                )
                // 로그아웃 성공 시 메인 페이지로 이동
                .logout(logout ->
                        logout.logoutSuccessUrl("/")
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 ->
                        oauth2.userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                );

        return http.build();
    }
}
