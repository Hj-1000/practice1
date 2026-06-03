package com.jeonhong.book.springboot.web;

import com.jeonhong.book.springboot.config.auth.LoginUser;
import com.jeonhong.book.springboot.config.auth.dto.SessionUser;
import com.jeonhong.book.springboot.service.review.ReviewService;
import com.jeonhong.book.springboot.service.s3.S3UploadService;
import com.jeonhong.book.springboot.web.dto.ReviewResponseDto;
import com.jeonhong.book.springboot.web.dto.ReviewSaveRequestDto;
import com.jeonhong.book.springboot.web.dto.ReviewUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReviewApiController {

    private final ReviewService reviewService;
    private final S3UploadService  s3UploadService;

    // 맛집 및 리뷰를 등록하는 api 엔드포인트
    @PostMapping("/api/v1/reviews")
    public Long save(@ModelAttribute ReviewSaveRequestDto requestDto,
                     @LoginUser SessionUser user) throws IOException {

        // 사용자가 리뷰 사진을 업로드했는지 검증한다
        if(requestDto.getImageFile() != null && !requestDto.getImageFile().isEmpty()){
            // S3의 'review-images'라는 폴더 경로로 사진을 업로드하고 공개 URL 주소를 받아옵니다.
            String uploadedUrl = s3UploadService.upload(requestDto.getImageFile(), "review-images");
            // 💡 2. 받아온 S3 URL 주소를 DTO의 imageUrl 필드에 밀어 넣습니다.
            requestDto.setImageUrl(uploadedUrl);
        }

        // SecurityConfig에서 인가된 사용자만 이 APU를 칠 수 있으므로
        // @LoginUser를 통해 세션에서 안전하게 유저 이메일을 꺼내 서비스로 토스
        return reviewService.save(requestDto, user.getEmail());
    }

    @PutMapping("/api/v1/reviews/{id}")
    public Long update(@PathVariable Long id, @RequestBody ReviewUpdateRequestDto requestDto){
        return reviewService.update(id, requestDto);
    }

    @DeleteMapping("/api/v1/reviews/{id}")
    public Long delete(@PathVariable Long id){
        reviewService.delete(id);
        return id;
    }

    @GetMapping("/api/v1/reviews/nearby")
    public ResponseEntity<List<ReviewResponseDto>> getNearbyReviews(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng,
            @RequestParam(value = "radius", defaultValue = "1.0") double radius) {
        List<ReviewResponseDto> nearbyReviews = reviewService.findNearby(lat, lng, radius);
        return ResponseEntity.ok(nearbyReviews);
    }

}
