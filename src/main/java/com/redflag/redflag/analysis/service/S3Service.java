package com.redflag.redflag.analysis.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    
    private final AmazonS3 amazonS3;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    
    @Value("${cloud.aws.region.static}")
    private String region;
    
    // 이미지 파일을 S3에 업로드하고 URL을 반환
    public String upload(MultipartFile file) {
        // 파일명 생성 (UUID로 중복 방지)
        String fileName = generateFileName(file.getOriginalFilename());
        
        // 파일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        
        try {
            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(
                bucket,
                fileName,
                file.getInputStream(),
                metadata
            ));
            
            // 업로드된 파일의 URL 생성
            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket, region, fileName);
            
            log.info("S3 업로드 완료: {}", imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
    
    // UUID를 사용한 고유 파일명 생성 (충돌 방지)
    // ex: analysis/550e8400-e29b-41d4-a716-446655440000.jpg
    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return "analysis/" + UUID.randomUUID() + extension;
    }
}
