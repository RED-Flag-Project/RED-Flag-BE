package com.redflag.redflag.analysis.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SimilarCaseDto {
    private Integer matchedRank;
    private String caseId;
    private String category;
    private BigDecimal similarityScore;
    private String content;
    private String highlightUser;
    private String highlightCase;
}
