package com.jeonhong.book.springboot.web;

import com.jeonhong.book.springboot.domain.place.Place;
import com.jeonhong.book.springboot.domain.place.PlaceRepository;
import com.jeonhong.book.springboot.domain.review.ReviewRepository;
import com.jeonhong.book.springboot.domain.user.Role;
import com.jeonhong.book.springboot.domain.user.User;
import com.jeonhong.book.springboot.domain.user.UserRepository;
import com.jeonhong.book.springboot.service.s3.S3UploadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter; // 한글 깨짐 방지용 (필요시 사용)

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @MockitoBean
    private S3UploadService s3UploadService;

    @BeforeEach
    public void setUp() throws IOException {
        // 1. 💡 [수정] mvc 객체 누락된 초기화 추가 (컨텍스트 기반 빌드)
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 파라미터 깨짐 방지
                .build();

        // 2. 💡 [오류 해결] upload 메서드의 파라미터 타입과 개수(2개)를 매칭해 줍니다.
        Mockito.when(s3UploadService.upload(Mockito.any(), Mockito.anyString()))
                .thenReturn("https://myspringboot-images.s3.ap-northeast-2.amazonaws.com/mock-image.png");
    }

    @AfterEach
    public void tearDown() throws Exception {
        reviewRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void Review_리뷰와_S3사진이_함께_등록된다() throws Exception {
        // given
        User user = userRepository.save(User.builder()
                .name("홍길동")
                .email("testuser@gmail.com")
                .role(Role.USER)
                .build());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new com.jeonhong.book.springboot.config.auth.dto.SessionUser(user));

        MockMultipartFile mockImageFile = new MockMultipartFile(
                "imageFile",
                "test-image.png",
                "image/png",
                "test-binary-data".getBytes()
        );

        String url = "http://localhost:" + port + "/api/v1/reviews";

        // when
        mvc.perform(multipart(url)
                        .file(mockImageFile)
                        .param("kakaoPlaceId", "123456")
                        .param("placeName", "테스트 존맛탱 맛집")
                        .param("category", "음식점 > 한식")
                        .param("addressName", "경기도 부천시 부천로")
                        .param("latitude", "37.5")
                        .param("longitude", "126.9")
                        .param("rating", "5")
                        .param("visitDate", LocalDate.now().toString())
                        .param("content", "진짜 너무 맛있어요. 강력 추천합니다!")
                        .session(session))
                .andExpect(status().isOk());

        // then
        var allReviews = reviewRepository.findAll();
        assertThat(allReviews.size()).isEqualTo(1);

        String savedImageUrl = allReviews.get(0).getImageUrl();
        System.out.println("=========================================");
        System.out.println("실제 S3 업로드된 이미지 주소: " + savedImageUrl);
        System.out.println("=========================================");

        assertThat(savedImageUrl).isNotNull();
        assertThat(savedImageUrl).contains("myspringboot-images");
    }
}