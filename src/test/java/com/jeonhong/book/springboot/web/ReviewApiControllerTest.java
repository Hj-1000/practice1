package com.jeonhong.book.springboot.web;

import com.jeonhong.book.springboot.domain.Image.ReviewImage;
import com.jeonhong.book.springboot.domain.Image.ReviewImageRepository;
import com.jeonhong.book.springboot.domain.place.Place;
import com.jeonhong.book.springboot.domain.place.PlaceRepository;
import com.jeonhong.book.springboot.domain.review.Review;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter; // 한글 깨짐 방지용 (필요시 사용)

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
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
    private ReviewImageRepository reviewImageRepository;

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
                .thenReturn(List.of("https://myspringboot-images.s3.ap-northeast-2.amazonaws.com/mock-image.png"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        reviewImageRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        placeRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @Transactional
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
                "imageFiles",
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
        // 1. 트랜잭션이 종료되기 전, DB에 저장된 내용을 반영하고 컨텍스트를 비웁니다.
        // 이것이 없으면 영속성 컨텍스트가 꼬여서 조회 시 빈 리스트가 나오거나 에러가 납니다.
        reviewRepository.flush();
        // 테스트 메서드에 @Transactional이 있으므로, 이 시점에 이미 DB에는 데이터가 반영되어 있습니다.

        var allReviews = reviewRepository.findAll();
        assertThat(allReviews.size()).isEqualTo(1);

        Review savedReview = allReviews.get(0);

        // 2. 이미지가 로딩되지 않았을 경우를 대비해, 직접 이미지를 조회해봅니다.
        List<ReviewImage> savedImages = reviewImageRepository.findAllByReviewId((savedReview.getId()));
        // ※ 만약 findAllByReviewId가 없다면 reviewImageRepository에 추가하거나,
        //    아래처럼 savedReview.getImages()를 쓰되 위에서 flush()를 꼭 호출하세요.

        assertThat(savedImages).isNotEmpty();

        String firstImageUrl = savedImages.get(0).getImageUrl();

        System.out.println("=========================================");
        System.out.println("실제 S3 업로드된 이미지 주소: " + firstImageUrl);
        System.out.println("=========================================");

        assertThat(firstImageUrl).isNotNull();
        assertThat(firstImageUrl).contains("myspringboot-images");
    }
}