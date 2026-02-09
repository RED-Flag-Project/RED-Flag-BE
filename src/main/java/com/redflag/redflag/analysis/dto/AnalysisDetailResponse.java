package com.redflag.redflag.analysis.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AnalysisDetailResponse {
    private String analysisId;
    private String imageUrl;
    private String rawText;
    private Integer riskScore;
    private String riskLevel;
    private String description;
    private List<PsychologicalPatternDto> psychologicalPatterns;
    private List<SimilarCaseDto> similarCases;
    private LocalDateTime createdAt;
}
