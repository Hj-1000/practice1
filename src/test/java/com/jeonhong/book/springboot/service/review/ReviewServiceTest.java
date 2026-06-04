package com.jeonhong.book.springboot.service.review;

import com.jeonhong.book.springboot.domain.place.Place;
import com.jeonhong.book.springboot.domain.place.PlaceRepository;
import com.jeonhong.book.springboot.domain.review.Review;
import com.jeonhong.book.springboot.domain.review.ReviewRepository;
import com.jeonhong.book.springboot.domain.user.Role;
import com.jeonhong.book.springboot.domain.user.User;
import com.jeonhong.book.springboot.domain.user.UserRepository;
import com.jeonhong.book.springboot.web.dto.ReviewSaveRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void cleanUp() {
        reviewRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void 맛집리뷰가_정상적으로_저장된다(){
        // given
        // 1. 가상의 로그인 유저 먼저 DB에 저장
        String email = "testuser@gmial.com";
        User user = userRepository.save(User.builder()
                        .name("홍길동")
                        .email(email)
                        .role(Role.USER)
                        .build());

        // 2 . 프론트엔드에서 날아올 가상의 DTO 데이터 준비(카멜케이스 검증)
        ReviewSaveRequestDto requestDto = ReviewSaveRequestDto.builder()
                .kakaoPlaceId("1234567")
                .placeName("부천 맛집 족발집")
                .category("음식점")
                .addressName("경기도 부천시 원미구")
                .latitude(37.498)
                .longitude(126.867)
                .rating(5)
                .visitDate(LocalDate.now())
                .content("인생 족발집입니다. 너무 맛있어요!")
                .imageUrls(null)
                .build();

        // when
        // 서비스의 save 로직 실행
        Long reviewId = reviewService.save(requestDto, email);

        // then
        // 1. 리뷰가 데이터베이스에 잘 들어갔는지 검증
        List<Review> reviewList = reviewRepository.findAll();
        Review review = reviewList.get(0);
        assertThat(review.getId()).isEqualTo(reviewId);
        assertThat(review.getContent()).isEqualTo("인생 족발집입니다. 너무 맛있어요!");
        assertThat(review.getVisitDate()).isEqualTo(LocalDate.now());
        assertThat(review.getUser().getEmail()).isEqualTo(email);

        // 2. 맛집(Place) 테이블에도 중복 없이 데이터가 잘 연동되었는지 검증
        List<Place> placeList = placeRepository.findAll();
        assertThat(placeList.size()).isEqualTo(1);
        assertThat(placeList.get(0).getKakaoPlaceId()).isEqualTo("1234567");
    }
}
