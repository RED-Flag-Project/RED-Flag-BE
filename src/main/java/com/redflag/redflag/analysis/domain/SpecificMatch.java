package com.redflag.redflag.analysis.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "specific_match")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AnalysisHistory analysisHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "example_case_id", nullable = false)
    private ExampleCase exampleCase;

    @Column(name = "similarity_score", precision = 5, scale = 2)
    private BigDecimal similarityScore;

    @Column(name = "matched_rank")
    private Integer matchedRank;

    @Column(name = "highlight_text_user", columnDefinition = "TEXT")
    private String highlightTextUser;

    @Column(name = "highlight_text_case", columnDefinition = "TEXT")
    private String highlightTextCase;
}