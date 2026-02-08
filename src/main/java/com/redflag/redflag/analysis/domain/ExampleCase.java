package com.redflag.redflag.analysis.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "example_case")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExampleCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_content", columnDefinition = "TEXT")
    private String caseContent;

    @Column(name = "category", length = 255)
    private String category;

    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embedding;
}
