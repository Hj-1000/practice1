package com.jeonhong.book.springboot.domain.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 카카오 고유 ID로 기존 등록 여부 확인
    Optional<Place> findByKakaoPlaceId(String kakaoPlaceId);

    @Query("SELECT p FROM Place p ORDER BY p.id DESC")
    List<Place> findAllDesc();
}
