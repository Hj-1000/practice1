package com.jeonhong.book.springboot.service.s3;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3UploadService {

    // 수동 등록해둔 S3Template 빈이 정상 주입됩니다.
    private final S3Template s3Template;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();

        try (InputStream inputStream = multipartFile.getInputStream()) {

            // 💡 1. 라이브러리 규격에 맞는 ObjectMetadata 빌더를 생성하고 켄텐츠 타입을 세팅합니다.
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(multipartFile.getContentType())
                    .build();

            // 💡 2. 4번째 파라미터로 String 대신 생성한 metadata 객체를 전달합니다.
            S3Resource s3Resource = s3Template.upload(bucket, fileName, inputStream, metadata);

            String uploadImageUrl = s3Resource.getURL().toString();
            log.info("S3 업로드 성공! URL : {}", uploadImageUrl);

            return uploadImageUrl;
        } catch (IOException e) {
            log.error("S3 파일 업로드 중 에러 발생", e);
            throw new IOException("S3 업로드 실패", e);
        }
    }

}
