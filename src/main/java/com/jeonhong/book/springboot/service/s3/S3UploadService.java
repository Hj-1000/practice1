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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3UploadService {

    // 수동 등록해둔 S3Template 빈이 정상 주입됩니다.
    private final S3Template s3Template;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 💡 1. 파일 1개를 업로드하는 핵심 로직 (private으로 분리)
    private String uploadSingleFile(MultipartFile multipartFile, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();

        try (InputStream inputStream = multipartFile.getInputStream()) {
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(multipartFile.getContentType())
                    .build();

            S3Resource s3Resource = s3Template.upload(bucket, fileName, inputStream, metadata);
            return s3Resource.getURL().toString();
        }
    }

    // 💡 2. 컨트롤러에서 리스트를 넘기면 각각 호출해서 URL 리스트를 반환
    public List<String> upload(List<MultipartFile> multipartFiles, String dirName) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            if (!file.isEmpty()) {
                uploadedUrls.add(uploadSingleFile(file, dirName));
            }
        }
        return uploadedUrls;
    }

}
