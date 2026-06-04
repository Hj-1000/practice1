package com.jeonhong.book.springboot.domain.review;

import com.jeonhong.book.springboot.domain.BaseTimeEntity;
import com.jeonhong.book.springboot.domain.Image.ReviewImage;
import com.jeonhong.book.springboot.domain.place.Place;
import com.jeonhong.book.springboot.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Getter
@NoArgsConstructor
@Entity
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    private Integer rating;
    private LocalDate visitDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public Review(Place place, User user, Integer rating, LocalDate visitDate, String content) {
        this.place = place;
        this.user = user;
        this.rating = rating;
        this.visitDate = visitDate;
        this.content = content;
    }

    // 리뷰 수정 로직
    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }

}
