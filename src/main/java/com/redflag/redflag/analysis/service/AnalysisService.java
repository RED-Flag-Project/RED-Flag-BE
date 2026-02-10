package com.redflag.redflag.analysis.service;

import com.redflag.redflag.analysis.domain.*;
import com.redflag.redflag.analysis.dto.*;
import com.redflag.redflag.analysis.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
    
    private final S3Service s3Service;
    private final MlService mlService;
    private final GeminiService geminiService;
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
        if (mlResult.getEmbedding() != null && mlResult.getEmbedding().length > 0) {
            searchAndSaveSimilarCases(analysisHistory, mlResult.getEmbedding());
        } else {
            log.warn("ML 응답에 임베딩이 없어 유사 사례 검색을 건너뜁니다.");
        }
        
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
    private void searchAndSaveSimilarCases(AnalysisHistory analysisHistory, float[] embedding) {
        log.info("유사 사례 검색 시작 - embedding 차원: {}", embedding.length);
        
        // 1. float[] → pgvector 문자열 형식으로 변환
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        String embeddingStr = sb.toString();
        
        log.debug("변환된 임베딩 문자열: {}...", embeddingStr.substring(0, Math.min(50, embeddingStr.length())));
        
        // 2. pgvector로 유사한 과거 사례 검색 (상위 3개, 거리값 포함)
        List<Object[]> results = exampleCaseRepository.findSimilarCasesWithDistance(embeddingStr, 3);
        
        if (results.isEmpty()) {
            log.warn("유사 사례를 찾지 못했습니다. ExampleCase 테이블에 데이터가 있는지 확인하세요.");
            return;
        }
        
        log.info("유사 사례 검색 완료: {}개 발견", results.size());
        
        // AnalysisDetail 직접 조회 (LAZY 로딩 문제 해결)
        List<AnalysisDetail> details = analysisDetailRepository.findByAnalysisHistory(analysisHistory);
        log.debug("AnalysisDetail 조회 완료: {}개", details.size());
        
        // 3. 검색된 사례들을 SpecificMatch로 저장
        List<SpecificMatch> matches = new ArrayList<>();
        int rank = 1;
        
        for (Object[] row : results) {
            UUID exampleId = (UUID) row[0];
            String caseContent = (String) row[1];
            Double distance = (Double) row[2];
            Double similarity = 1.0 - distance;
            
            log.debug("매칭된 사례: ID={}, 거리={}, 유사도={}", 
                    exampleId, 
                    String.format("%.4f", distance),
                    String.format("%.2f%%", similarity * 100));
            
            // highlightTextUser: AnalysisDetail의 keyword 사용
            String highlightUser = details.stream()
                    .map(AnalysisDetail::getKeyword)
                    .filter(k -> k != null && !k.isEmpty())
                    .collect(Collectors.joining(", "));

            // detectedSentences 수집 (Gemini에 추가 컨텍스트 제공)
            List<String> detectedSentences = details.stream()
                    .map(AnalysisDetail::getDetectedSentence)
                    .filter(s -> s != null && !s.isEmpty())
                    .toList();

            // highlightTextCase: case_content에서 키워드 찾기
            String highlightCase = geminiService.extractSimilarKeywords(
                    caseContent,
                    highlightUser,
                    detectedSentences);
            
            log.info("Gemini 추출 결과: {}", highlightCase);
            
            ExampleCase exampleCaseRef = ExampleCase.builder()
                    .id(exampleId)
                    .build();
            
            SpecificMatch match = SpecificMatch.builder()
                    .analysisHistory(analysisHistory)
                    .exampleCase(exampleCaseRef)
                    .similarityScore(BigDecimal.valueOf(similarity))
                    .matchedRank(rank++)
                    .highlightTextUser(highlightUser)
                    .highlightTextCase(highlightCase)
                    .build();
            
            matches.add(match);
        }
        
        specificMatchRepository.saveAll(matches);
        log.info("유사 사례 매칭 완료: {}개 저장됨", matches.size());
    }
    
    // 분석 결과 상세 조회
    @Transactional(readOnly = true)
    public AnalysisDetailResponse getAnalysisDetail(String userUuidStr, String analysisIdStr) {
        log.info("분석 결과 상세 조회 - 사용자: {}, analysisId: {}", userUuidStr, analysisIdStr);
        
        UUID userUuid = UUID.fromString(userUuidStr);
        UUID analysisId = UUID.fromString(analysisIdStr);
        
        // 1. AnalysisHistory 조회 (사용자 검증 포함)
        AnalysisHistory analysisHistory = analysisHistoryRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과를 찾을 수 없습니다."));
        
        // 2. 사용자 권한 검증
        if (!analysisHistory.getUser().getId().equals(userUuid)) {
            throw new IllegalArgumentException("해당 분석 결과에 접근 권한이 없습니다.");
        }
        
        // 3. AnalysisDetail 조회 (심리 조작 패턴들)
        List<AnalysisDetail> details = analysisDetailRepository.findByAnalysisHistory(analysisHistory);
        List<PsychologicalPatternDto> psychologicalPatterns = details.stream()
                .map(detail -> PsychologicalPatternDto.builder()
                        .patternType(detail.getPatternType())
                        .detectedSentence(detail.getDetectedSentence())
                        .keyword(detail.getKeyword())
                        .patternScore(detail.getPatternScore())
                        .build())
                .collect(Collectors.toList());
        
        // 4. SpecificMatch 조회 (유사 사례들) - embedding 제외한 필드만 조회
        List<Object[]> matchResults = specificMatchRepository.findMatchDetailsWithoutEmbedding(analysisId);
        List<SimilarCaseDto> similarCases = matchResults.stream()
                .map(row -> SimilarCaseDto.builder()
                        .matchedRank((Integer) row[0])
                        .caseId(((UUID) row[1]).toString())
                        .category((String) row[2])
                        .content((String) row[3])
                        .similarityScore((BigDecimal) row[4])
                        .highlightUser((String) row[5])
                        .highlightCase((String) row[6])
                        .build())
                .collect(Collectors.toList());
        
        // 5. 응답 생성
        return AnalysisDetailResponse.builder()
                .analysisId(analysisHistory.getId().toString())
                .imageUrl(analysisHistory.getImageUrl())
                .rawText(analysisHistory.getRawText())
                .riskScore(analysisHistory.getRiskScore())
                .riskLevel(analysisHistory.getRiskLevel())
                .description(analysisHistory.getDescription())
                .psychologicalPatterns(psychologicalPatterns)
                .similarCases(similarCases)
                .createdAt(analysisHistory.getCreatedAt())
                .build();
    }
}
