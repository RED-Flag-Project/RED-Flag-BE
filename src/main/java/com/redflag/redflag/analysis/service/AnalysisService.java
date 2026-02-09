package com.redflag.redflag.analysis.service;

import com.redflag.redflag.analysis.domain.*;
import com.redflag.redflag.analysis.dto.AnalysisUploadResponse;
import com.redflag.redflag.analysis.dto.MlAnalysisResponse;
import com.redflag.redflag.analysis.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
    
    private final S3Service s3Service;
    private final MlService mlService;
    private final UserRepository userRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final AnalysisDetailRepository analysisDetailRepository;
    private final ExampleCaseRepository exampleCaseRepository;
    private final SpecificMatchRepository specificMatchRepository;
    
    // 이미지 업로드 및 분석 전체 프로세스
    @Transactional
    public AnalysisUploadResponse uploadAndAnalyze(String userUuidStr, MultipartFile image) {
        log.info("분석 시작 - 사용자: {}", userUuidStr);
        
        // 0. 이미지 파일 검증
        validateImage(image);
        
        // 1. User 조회 또는 생성
        UUID userUuid = UUID.fromString(userUuidStr);
        User user = userRepository.findById(userUuid)
                .orElseGet(() -> createNewUser(userUuid));
        
        // 2. S3에 이미지 업로드
        log.info("S3 업로드 시작");
        String imageUrl = s3Service.upload(image);
        log.info("S3 업로드 완료: {}", imageUrl);
        
        // 3. ML 서버에 분석 요청
        log.info("ML 분석 요청 시작");
        MlAnalysisResponse mlResult = mlService.analyze(image);
        log.info("ML 분석 완료 - riskScore: {}", mlResult.getRiskScore());
        
        // 4. AnalysisHistory 저장
        AnalysisHistory analysisHistory = saveAnalysisHistory(user, imageUrl, mlResult);
        
        // 5. AnalysisDetail 저장 (심리 조작 패턴들)
        saveAnalysisDetails(analysisHistory, mlResult.getPsychologicalPatterns());
        
        // 6. 벡터 유사도 검색 및 SpecificMatch 저장
        // TODO: ML 결과에 embedding이 포함되면 구현
        // searchAndSaveSimilarCases(analysisHistory, mlResult.getEmbedding());
        
        log.info("분석 완료 - analysisId: {}", analysisHistory.getId());
        
        // 7. 응답 생성
        return AnalysisUploadResponse.builder()
                .analysisId(analysisHistory.getId())  // UUID로 직접 반환
                .imageUrl(imageUrl)
                .build();
    }
    
    // 이미지 파일 검증
    private void validateImage(MultipartFile image) {
        if (image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }
        
        // 파일 크기 검증 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 파일 크기는 10MB를 초과할 수 없습니다.");
        }
        
        // 파일 타입 검증
        String contentType = image.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("이미지 파일 형식을 확인할 수 없습니다.");
        }
        
        if (!contentType.equals("image/jpeg") && 
            !contentType.equals("image/jpg") && 
            !contentType.equals("image/png")) {
            throw new IllegalArgumentException("JPG, PNG 형식의 이미지만 업로드 가능합니다.");
        }
    }
    
    // User 생성
    private User createNewUser(UUID userUuid) {
        User newUser = User.builder()
                .id(userUuid)
                .build();
        return userRepository.save(newUser);
    }
    
    // AnalysisHistory 저장
    private AnalysisHistory saveAnalysisHistory(User user, String imageUrl, MlAnalysisResponse mlResult) {
        AnalysisHistory analysisHistory = AnalysisHistory.builder()
                .user(user)
                .imageUrl(imageUrl)
                .rawText(mlResult.getOcrText())
                .riskScore(mlResult.getRiskScore())
                .riskLevel(mlResult.getRiskLevel())
                .description(mlResult.getDescription())
                .build();
        
        return analysisHistoryRepository.save(analysisHistory);
    }
    
    // AnalysisDetail 저장 (심리 조작 패턴들)
    private void saveAnalysisDetails(AnalysisHistory analysisHistory, 
                                     List<MlAnalysisResponse.PsychologicalPattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            log.warn("심리 조작 패턴이 없습니다.");
            return;
        }
        
        List<AnalysisDetail> details = patterns.stream()
                .map(pattern -> AnalysisDetail.builder()
                        .analysisHistory(analysisHistory)
                        .patternType(pattern.getPatternType())
                        .patternScore(pattern.getPatternScore())
                        .detectedSentence(pattern.getDetectedSentence())
                        .keyword(pattern.getKeyword())
                        .build())
                .toList();
        
        analysisDetailRepository.saveAll(details);
        log.info("AnalysisDetail 저장 완료: {}개", details.size());
    }
    
    // 벡터 유사도 검색 및 SpecificMatch 저장
    // TODO: ML 결과에 embedding 포함되면 활성화
    /*
    private void searchAndSaveSimilarCases(AnalysisHistory analysisHistory, float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            log.warn("임베딩 벡터가 없습니다.");
            return;
        }
        
        // pgvector 형식으로 변환 ("[0.1,0.2,0.3,...]")
        String embeddingStr = Arrays.stream(embedding)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
        
        // 유사한 사례 검색 (상위 5개)
        List<ExampleCase> similarCases = exampleCaseRepository.findSimilarCases(embeddingStr, 5);
        
        if (similarCases.isEmpty()) {
            log.warn("유사 사례를 찾지 못했습니다.");
            return;
        }
        
        // SpecificMatch 저장
        List<SpecificMatch> matches = similarCases.stream()
                .map(exampleCase -> SpecificMatch.builder()
                        .analysisHistory(analysisHistory)
                        .exampleCase(exampleCase)
                        .similarity(calculateSimilarity(embedding, exampleCase.getEmbedding()))
                        .build())
                .toList();
        
        specificMatchRepository.saveAll(matches);
        log.info("유사 사례 매칭 완료: {}개", matches.size());
    }
    
    private Double calculateSimilarity(float[] embedding1, float[] embedding2) {
        // 코사인 유사도 계산 로직
        // TODO: 구현 필요
        return 0.0;
    }
    */
}
