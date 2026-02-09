package com.redflag.redflag.analysis.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PsychologicalPatternDto {
    private String patternType;
    private String detectedSentence;
    private String keyword;
    private Integer patternScore;
}
