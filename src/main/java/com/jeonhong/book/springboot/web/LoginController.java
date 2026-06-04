package com.jeonhong.book.springboot.web;

import com.jeonhong.book.springboot.config.auth.dto.SessionUser;
import com.jeonhong.book.springboot.domain.user.User;
import com.jeonhong.book.springboot.domain.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final UserRepository userRepository; // User 조회용

    @PostMapping("/login/demo")
    public String demoLogin(HttpServletRequest request) {
        // 1. DB에서 데모 유저 조회
        User user = userRepository.findByEmail("demo@example.com")
                .orElseThrow(() -> new IllegalArgumentException("데모 사용자가 없습니다."));

        // 2. 세션용 DTO 생성
        SessionUser sessionUser = new SessionUser(user);

        // 3. Security Context 설정 (권한 부여)
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                sessionUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. 세션 설정 (💡 이 부분이 템플릿 출력의 핵심입니다)
        HttpSession session = request.getSession(true);
        // Mustache 템플릿에서 {{user}} 또는 {{userName}}을 찾을 수 있도록 "user" 속성을 넣어줍니다.
        session.setAttribute("user", sessionUser);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return "redirect:/";
    }
}
