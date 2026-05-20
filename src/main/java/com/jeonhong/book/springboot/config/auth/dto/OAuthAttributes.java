package com.jeonhong.book.springboot.config.auth.dto;

import com.jeonhong.book.springboot.domain.user.Role;
import com.jeonhong.book.springboot.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }

    public static OAuthAttributes of(String registrationId,
                                     String usernameAttributeName,
                                     Map<String, Object> attributes){
        if("naver".equals(registrationId)){
            return ofNaver("id", attributes);
        }
        return ofGoogle(usernameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String usernameAttributeName, Map<String, Object> attributes){
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(usernameAttributeName)
                .build();

    }

        private static OAuthAttributes ofNaver(String usernameAttributeName, Map<String, Object> attributes){

        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .attributes(response)
                .nameAttributeKey(usernameAttributeName)
                .build();

    }

    public User toEntity(){
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.USER) // 테스트를 쉽게 하기 위해 로그인 하면 바로 USER 인걸로 했음 기존 Role.GUEST
                .build();
    }
}
