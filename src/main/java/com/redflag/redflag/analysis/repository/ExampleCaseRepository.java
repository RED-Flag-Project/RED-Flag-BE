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
    
    // pgvector를 사용한 유사도 검색 (코사인 유사도 활용)
    // embedding 컬럼과 주어진 벡터의 코사인 유사도를 계산하여 유사한 사례 검색
    @Query(value = "SELECT * FROM example_case " +
                   "ORDER BY embedding <=> CAST(:embedding AS vector) " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<ExampleCase> findSimilarCases(@Param("embedding") String embedding, 
                                       @Param("limit") int limit);
}
