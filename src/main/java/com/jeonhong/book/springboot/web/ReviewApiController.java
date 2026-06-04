package com.jeonhong.book.springboot.web;

import com.jeonhong.book.springboot.config.auth.LoginUser;
import com.jeonhong.book.springboot.config.auth.dto.SessionUser;
import com.jeonhong.book.springboot.service.review.ReviewService;
import com.jeonhong.book.springboot.service.s3.S3UploadService;
import com.jeonhong.book.springboot.web.dto.ReviewResponseDto;
import com.jeonhong.book.springboot.web.dto.ReviewSaveRequestDto;
import com.jeonhong.book.springboot.web.dto.ReviewUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
public class ReviewApiController {

    private final ReviewService reviewService;
    private final S3UploadService  s3UploadService;

    // 맛집 및 리뷰를 등록하는 api 엔드포인트
    @PostMapping("/api/v1/reviews")
    public Long save(@ModelAttribute ReviewSaveRequestDto requestDto,
                     @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                     @LoginUser SessionUser user) throws IOException {

        // 💡 1. 여러 장의 사진을 업로드하고 URL 리스트를 받아옴
        if(imageFiles != null && !imageFiles.isEmpty()){
            List<String> uploadedUrls = s3UploadService.upload(imageFiles, "review-images");
            // 💡 2. 이제 DTO에 URL 리스트를 담아야 합니다 (DTO 수정 필요!)
            requestDto.setImageUrls(uploadedUrls);
        }

        log.info("들어온 requestDto: " + requestDto);
        log.info("업로드할 파일 개수: {}", imageFiles.size());


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
