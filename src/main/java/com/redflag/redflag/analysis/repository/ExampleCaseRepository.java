package com.redflag.redflag.analysis.repository;

import com.redflag.redflag.analysis.domain.ExampleCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExampleCaseRepository extends JpaRepository<ExampleCase, UUID> {
    
    // pgvector를 사용한 유사도 검색 (코사인 거리값 포함)
    // embedding column과 주어진 벡터의 코사인 거리를 계산하여 유사한 사례 검색
    // 반환값: [id, case_content, distance]
    @Query(value = "SELECT " +
                   "id, " +
                   "case_content, " +
                   "(embedding <=> CAST(:embedding AS vector)) as distance " +
                   "FROM example_case " +
                   "ORDER BY distance " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Object[]> findSimilarCasesWithDistance(@Param("embedding") String embedding, 
                                                @Param("limit") int limit);
}
