package com.redflag.redflag.analysis.repository;

import com.redflag.redflag.analysis.domain.AnalysisHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {

    /**
     * 특정 날짜 범위 내에서 riskScore가 특정 값 이상인 데이터 개수 조회
     */
    @Query("SELECT COUNT(ah) FROM AnalysisHistory ah " +
           "WHERE ah.riskScore >= :minRiskScore " +
           "AND ah.createdAt >= :startDate " +
           "AND ah.createdAt < :endDate")
    Long countByRiskScoreGreaterThanEqualAndCreatedAtBetween(
            @Param("minRiskScore") Integer minRiskScore,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
