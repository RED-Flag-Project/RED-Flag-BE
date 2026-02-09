package com.redflag.redflag.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisUploadResponse {
    
    private UUID analysisId;
    private String imageUrl;
}
