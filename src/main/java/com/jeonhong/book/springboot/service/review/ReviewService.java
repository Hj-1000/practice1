package com.jeonhong.book.springboot.service.review;

import com.jeonhong.book.springboot.domain.Image.ReviewImage;
import com.jeonhong.book.springboot.domain.Image.ReviewImageRepository;
import com.jeonhong.book.springboot.domain.place.Place;
import com.jeonhong.book.springboot.domain.place.PlaceRepository;
import com.jeonhong.book.springboot.domain.review.Review;
import com.jeonhong.book.springboot.domain.review.ReviewRepository;
import com.jeonhong.book.springboot.domain.user.User;
import com.jeonhong.book.springboot.domain.user.UserRepository;
import com.jeonhong.book.springboot.web.dto.ReviewResponseDto;
import com.jeonhong.book.springboot.web.dto.ReviewSaveRequestDto;
import com.jeonhong.book.springboot.web.dto.ReviewUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final PlaceRepository placeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository; //로그인한 유저를 맵핑하기 위해서
    private final ReviewImageRepository reviewImageRepository;

    @Transactional
    public Long save(ReviewSaveRequestDto requestDto, String loginUserEmail) {

        // 1. 현재 로그인하여 리뷰를 남기는 유저 조회
        User user = userRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다." + loginUserEmail));

        // 2. 이미 등록된 맛집인지 확인하고, 없으면 새로 등록 (중복 방지 매커니즘)
        Place place = placeRepository.findByKakaoPlaceId(requestDto.getKakaoPlaceId())
                .orElseGet(()-> placeRepository.save(Place.builder()
                        .kakaoPlaceId(requestDto.getKakaoPlaceId())
                        .placeName(requestDto.getPlaceName())
                        .category(requestDto.getCategory())
                        .addressName(requestDto.getAddressName())
                        .latitude(requestDto.getLatitude())
                        .longitude(requestDto.getLongitude())
                        .build()));

        // 💡 1. 리뷰 엔티티 생성 시 imageUrl 삭제 (빌더에서 제거)
        Review review = reviewRepository.save(Review.builder()
                .place(place)
                .user(user)
                .rating(requestDto.getRating())
                .visitDate(requestDto.getVisitDate())
                .content(requestDto.getContent())
                .build());

        // 💡 2. 리스트로 넘어온 URL들을 반복문으로 저장
        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            for (String url : requestDto.getImageUrls()) {
                reviewImageRepository.save(ReviewImage.builder()
                        .imageUrl(url)
                        .review(review) // 연관관계 설정
                        .build());
            }
        }

        return review.getId();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> findAllDesc(){
        return reviewRepository.findAllDesc().stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto findById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 맛집 리뷰가 존재하지 않습니다. id=" + id));
        return new ReviewResponseDto(review);
    }

    @Transactional(readOnly = true)
    public Long update(Long id, ReviewUpdateRequestDto requestDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 맛집 리뷰가 존재하지 않습니다. id=" + id));

        // 엔티티 내부의 update 메서드를 수행하여 더티 체킹(Dirty Checking)으로 DB 수정
        review.update(requestDto.getRating(), requestDto.getContent());
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 맛집 리뷰가 존재하지 않습니다."));
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> findNearby(double lat, double lng, double radius) {
        // 1. 네이티브 쿼리로 리뷰 리스트 조회
        List<Review> reviews = reviewRepository.findNearbyReviews(lat, lng, radius);

        // 2. 각 리뷰에 대해 이미지가 로드되도록 강제 호출 (HibernateProxy 해제)
        reviews.forEach(review -> review.getImages().size());

        reviews.forEach(review -> {
            int size = review.getImages().size();
            log.info("리뷰 ID: {} 의 이미지 개수: {}", review.getId(), size);
        });

        // 3. DTO 변환
        return reviews.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

}
