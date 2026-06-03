package com.jeonhong.book.springboot.web;

import com.jeonhong.book.springboot.config.auth.LoginUser;
import com.jeonhong.book.springboot.config.auth.dto.SessionUser;
import com.jeonhong.book.springboot.service.posts.PostsService;
import com.jeonhong.book.springboot.service.review.ReviewService;
import com.jeonhong.book.springboot.web.dto.PostsResponseDto;
import com.jeonhong.book.springboot.web.dto.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        if (user != null) {
            model.addAttribute("userName", user.getName());
        }
        return "index"; // 이제 3단계에서 새로 만들 자기소개 화면으로 연결됩니다.
    }

    // 2. [격리] 기존 책의 스프링 부트 게시판 메인 화면 주소 분리
    @GetMapping("/posts/board")
    public String boardMain(Model model, @LoginUser SessionUser user) {
        model.addAttribute("posts", postsService.findAllDesc());
        if (user != null) {
            model.addAttribute("userName", user.getName());
        }
        return "board-main";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model) {
        PostsResponseDto dto = postsService.findById(id);
        model.addAttribute("post", dto);

        return "posts-update";
    }

    @GetMapping("/review/save")
    public String reviewSave(){
        return "review-save";
    }

    @GetMapping("/reviews")
    public String reviewMain(Model model, @LoginUser SessionUser user) {
        List<ReviewResponseDto> reviews = reviewService.findAllDesc();
        // 맛집 전체 목록 조회해서 모델에 담기
        model.addAttribute("reviews", reviews);

        // 맛집 리스트를 자바스크립트가 바로 읽을 수 있도록 JSON 문자열로 캐스팅
        try {
            String reviewsJson = objectMapper.writeValueAsString(reviews);
            model.addAttribute("reviewsJson", reviewsJson);
        }catch (Exception e){
            model.addAttribute("reviewsJson", "[]");
        }

        if (user != null) {
            model.addAttribute("userName", user.getName());
        }
        return "review-main"; // 방금 만든 review-main.mustache를 뷰로 지정
    }

    @GetMapping("/reviews/update/{id}")
    public String reviewUpdate(@PathVariable Long id, Model model, @LoginUser SessionUser user) {
        ReviewResponseDto dto = reviewService.findById(id);
        model.addAttribute("review", dto);

        if (user != null) {
            model.addAttribute("userName", user.getName());
        }
        return "review-update";
    }

}
