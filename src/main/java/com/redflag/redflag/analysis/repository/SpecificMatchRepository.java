package com.redflag.redflag.analysis.repository;

import com.redflag.redflag.analysis.domain.SpecificMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecificMatchRepository extends JpaRepository<SpecificMatch, UUID> {
    
    /**
     * 특정 분석 ID에 해당하는 유사 사례들을 매칭 순위 오름차순으로 조회
     * embedding 필드를 제외하고 필요한 필드만 조회 (역직렬화 에러 방지)
     * 
     * @return Object[] 구조:
     *   [0] Integer matchedRank
     *   [1] UUID caseId
     *   [2] String category
     *   [3] String caseContent
     *   [4] BigDecimal similarityScore
     *   [5] String highlightTextUser
     *   [6] String highlightTextCase
     */
    @Query("SELECT sm.matchedRank, sm.exampleCase.id, sm.exampleCase.category, " +
           "sm.exampleCase.caseContent, sm.similarityScore, " +
           "sm.highlightTextUser, sm.highlightTextCase " +
           "FROM SpecificMatch sm " +
           "WHERE sm.analysisHistory.id = :analysisId " +
           "ORDER BY sm.matchedRank ASC")
    List<Object[]> findMatchDetailsWithoutEmbedding(@Param("analysisId") UUID analysisId);
}
