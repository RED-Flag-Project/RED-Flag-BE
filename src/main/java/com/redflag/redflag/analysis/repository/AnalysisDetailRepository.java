package com.redflag.redflag.analysis.repository;

import com.redflag.redflag.analysis.domain.AnalysisDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnalysisDetailRepository extends JpaRepository<AnalysisDetail, UUID> {
}
