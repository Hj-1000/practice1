package com.jeonhong.book.springboot.service.review;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final PlaceRepository placeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository; //로그인한 유저를 맵핑하기 위해서

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

        // 3. 빌더 패턴을 이용해 Review 엔티티 생성하고 저장
        Review review = reviewRepository.save(Review.builder()
                .place(place)
                .user(user)
                .rating(requestDto.getRating())
                .visitDate(requestDto.getVisitDate())
                .content(requestDto.getContent())
                .imageUrl(requestDto.getImageUrl())
                .build());

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
        review.update(requestDto.getRating(), requestDto.getContent(), requestDto.getImageUrl());
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 맛집 리뷰가 존재하지 않습니다."));
        reviewRepository.delete(review);
    }

    public List<ReviewResponseDto> findNearby(double lat, double lng, double radius) {
        return reviewRepository.findNearbyReviews(lat, lng, radius).stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

}
