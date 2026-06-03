package com.jeonhong.book.springboot.domain.review;

import com.jeonhong.book.springboot.domain.posts.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r ORDER BY r.id DESC")
    List<Review> findAllDesc();

    // Place 테이블과 JOIN하여 위경도를 가져온 뒤 내 주변 반경 맛집을 계산하는 쿼리
    @Query(value = "SELECT * FROM (" +
            "    SELECT r.*, " +
            "           (6371 * acos(cos(radians(:currentLat)) * cos(radians(p.latitude)) " +
            "           * cos(radians(p.longitude) - radians(:currentLng)) " +
            "           + sin(radians(:currentLat)) * sin(radians(p.latitude)))) AS distance " +
            "    FROM review r " +
            "    INNER JOIN place p ON r.place_id = p.id " +
            "    WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL " +
            "      AND p.latitude BETWEEN 33 AND 43 AND p.longitude BETWEEN 124 AND 132" +
            ") AS near_reviews " +
            "WHERE near_reviews.distance <= :radius " +
            "ORDER BY near_reviews.distance ASC", nativeQuery = true)
    List<Review> findNearbyReviews(@Param("currentLat") double currentLat,
                                   @Param("currentLng") double currentLng,
                                   @Param("radius") double radius);
}
