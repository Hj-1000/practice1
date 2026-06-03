package com.jeonhong.book.springboot.domain.place;

import com.jeonhong.book.springboot.domain.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter     // Entity class에서는 Setter를 만들지 않는다.
@NoArgsConstructor
@Entity
public class Place extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kakaoPlaceId;
    private String placeName;
    private String category;
    private String addressName;
    private Double latitude;
    private Double longitude;

    @Builder
    public Place(String kakaoPlaceId, String placeName, String category, String addressName, Double latitude, Double longitude) {
        this.kakaoPlaceId = kakaoPlaceId;
        this.placeName = placeName;
        this.category = category;
        this.addressName = addressName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
