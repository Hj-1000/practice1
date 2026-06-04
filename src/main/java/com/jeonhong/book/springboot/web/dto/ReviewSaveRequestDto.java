package com.jeonhong.book.springboot.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReviewSaveRequestDto {

    // Place 정보
    private String kakaoPlaceId;
    private String placeName;
    private String category;
    private String addressName;
    private Double latitude;
    private Double longitude;

    // Review 정보
    private Integer rating;
    private LocalDate visitDate;
    private String content;
    private List<String> imageUrls = new ArrayList<>();

    // 💡 프론트에서 전송한 실제 사진 파일을 임시로 담을 필드 추가
    private List<MultipartFile> imageFile;

    @Builder
    public ReviewSaveRequestDto(String kakaoPlaceId, String placeName, String category, String addressName,
                                Double latitude, Double longitude, Integer rating, LocalDate visitDate,
                                String content, List<String> imageUrls, List<MultipartFile> imageFiles) {
        this.kakaoPlaceId = kakaoPlaceId;
        this.placeName = placeName;
        this.category = category;
        this.addressName = addressName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.visitDate = visitDate;
        this.content = content;
        this.imageUrls = imageUrls;
        this.imageFile = imageFiles;
    }

}
