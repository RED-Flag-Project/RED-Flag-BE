package com.redflag.redflag.analysis.domain;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "example_case")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExampleCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_content", columnDefinition = "TEXT")
    private String caseContent;

    @Column(name = "category", length = 255)
    private String category;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private PGvector embedding;
}