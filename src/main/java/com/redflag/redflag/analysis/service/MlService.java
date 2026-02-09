package com.redflag.redflag.analysis.service;

import com.redflag.redflag.analysis.dto.MlAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MlService {
    
    private final RestTemplate restTemplate;
    
    @Value("${ml.server.url}")
    private String mlServerUrl;
    
    // ML 서버에 이미지를 전송하고 분석 결과를 받아옴
    public MlAnalysisResponse analyze(MultipartFile image) {
        try {
            // 1. multipart/form-data 요청 body 생성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });
            
            // 2. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // 3. HTTP 요청 생성
            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
            
            // 4. ML 서버로 POST 요청
            log.info("ML 서버 분석 요청: {}", mlServerUrl);
            ResponseEntity<MlAnalysisResponse> response = restTemplate.postForEntity(
                mlServerUrl,
                requestEntity,
                MlAnalysisResponse.class
            );
            
            // 5. 응답 확인
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("ML 분석 완료: riskScore={}", response.getBody().getRiskScore());
                return response.getBody();
            } else {
                log.error("ML 서버 응답 오류: status={}", response.getStatusCode());
                throw new RuntimeException("ML 분석 실패");
            }
            
        } catch (IOException e) {
            log.error("이미지 파일 읽기 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 처리 실패", e);
        } catch (Exception e) {
            log.error("ML 서버 통신 실패: {}", e.getMessage());
            throw new RuntimeException("ML 서버 연동 실패", e);
        }
    }
}
