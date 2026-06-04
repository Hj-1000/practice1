package com.jeonhong.book.springboot.config;

import com.jeonhong.book.springboot.domain.user.Role;
import com.jeonhong.book.springboot.domain.user.User;
import com.jeonhong.book.springboot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class DemoDataInitializer {
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.findByEmail("demo@example.com").isEmpty()) {
                userRepository.save(User.builder()
                        .name("Guest")
                        .email("demo@example.com")
                        .picture("https://img.icons8.com/color/96/guest-male.png")
                        .role(Role.USER) // 혹은 Role.GUEST 등 본인의 Role 설정
                        .build());
            }
        };
    }
}
