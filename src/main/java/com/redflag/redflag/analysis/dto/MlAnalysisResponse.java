package com.redflag.redflag.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlAnalysisResponse {
    
    // 분석 ID
    private UUID analysisId;
    
    // OCR 추출 텍스트
    private String ocrText;
    
    // AI 위험 지수 (0-100)
    private Integer riskScore;
    
    // 위험 등급
    private String riskLevel;
    
    // 분석 결과 설명
    private String description;
    
    // 심리 조작 패턴 목록
    private List<PsychologicalPattern> psychologicalPatterns;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PsychologicalPattern {
        // 패턴 유형
        private String patternType;
        
        // 탐지된 원문 문장
        private String detectedSentence;
        
        // 핵심 키워드
        private String keyword;
        
        // 패턴 점수
        private Integer patternScore;
    }
}
