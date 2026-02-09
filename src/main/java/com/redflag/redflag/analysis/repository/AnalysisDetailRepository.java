package com.redflag.redflag.analysis.repository;

import com.redflag.redflag.analysis.domain.AnalysisDetail;
import com.redflag.redflag.analysis.domain.AnalysisHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisDetailRepository extends JpaRepository<AnalysisDetail, UUID> {
    
    // AnalysisHistory로 AnalysisDetail 조회
    List<AnalysisDetail> findByAnalysisHistory(AnalysisHistory analysisHistory);
}
