package com.jeonhong.book.springboot.web.dto;

import com.jeonhong.book.springboot.domain.review.Review;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ReviewResponseDto {

    private Long id;
    private String placeName;
    private String category;
    private String addressName;
    private Double latitude;
    private Double longitude;
    private String ratingStars; // 숫자를 별(⭐)로 변환한 문자열
    private String visitDate;   // 머스타치에서 포맷팅하기 편하도록 String 변환
    private String content;
    private List<String> imageUrls;

    public ReviewResponseDto(Review entity) {
        this.id = entity.getId();
        this.placeName = entity.getPlace().getPlaceName();
        this.category = entity.getPlace().getCategory();
        this.addressName = entity.getPlace().getAddressName();
        this.latitude = entity.getPlace().getLatitude();
        this.longitude = entity.getPlace().getLongitude();
        this.content = entity.getContent();
        this.imageUrls = entity.getImages().stream()
                .map(image -> image.getImageUrl())
                .collect(Collectors.toList());
        // 💡 여기서 로그를 찍어보세요!
        System.out.println("DTO 생성 중 - 이미지 URL 개수: " + (this.imageUrls != null ? this.imageUrls.size() : "null"));
        // ... 기존 코드 ...

        // 날짜 예쁘게 변환 (예: 2026.06.02)
        if (entity.getVisitDate() != null) {
            this.visitDate = entity.getVisitDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }

        // 평점 숫자를 별 모양 문자열로 스위칭
        if (entity.getRating() != null) {
            this.ratingStars = "⭐".repeat(Math.max(0, entity.getRating()));
        }
    }
}
