package com.redflag.redflag.analysis.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "analysis_detail",
        indexes = @Index(name = "idx_analysis_detail_analysis_id", columnList = "analysis_id"))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AnalysisHistory analysisHistory;

    @Column(name = "pattern_type", length = 255)
    private String patternType;

    @Column(name = "pattern_score")
    private Integer patternScore;

    @Column(name = "detected_sentence", columnDefinition = "TEXT")
    private String detectedSentence;

    @Column(name = "keyword", length = 255)
    private String keyword;
}
