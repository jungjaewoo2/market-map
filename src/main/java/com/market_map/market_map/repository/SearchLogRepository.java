package com.market_map.market_map.repository;

import com.market_map.market_map.entity.SearchLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    
    /**
     * 최근 검색 로그 조회
     * @param pageable 페이지 정보
     * @return Page<SearchLog>
     */
    Page<SearchLog> findByOrderBySearchedAtDesc(Pageable pageable);
    
    /**
     * 특정 기간 검색 로그 조회
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return List<SearchLog>
     */
    List<SearchLog> findBySearchedAtBetweenOrderBySearchedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 검색 키워드별 통계
     * @return List<Object[]>
     */
    @Query("SELECT sl.searchKeyword, COUNT(sl) as count FROM SearchLog sl " +
           "WHERE sl.searchKeyword IS NOT NULL " +
           "GROUP BY sl.searchKeyword ORDER BY count DESC")
    List<Object[]> findSearchKeywordStatistics();
    
    /**
     * 검색 타입별 통계
     * @return List<Object[]>
     */
    @Query("SELECT sl.searchType, COUNT(sl) as count FROM SearchLog sl " +
           "WHERE sl.searchType IS NOT NULL " +
           "GROUP BY sl.searchType ORDER BY count DESC")
    List<Object[]> findSearchTypeStatistics();
    
    /**
     * 특정 키워드의 검색 횟수
     * @param keyword 검색 키워드
     * @return long
     */
    long countBySearchKeyword(String keyword);
    
    /**
     * 오늘의 검색 로그 수
     * @return long
     */
    @Query("SELECT COUNT(sl) FROM SearchLog sl WHERE DATE(sl.searchedAt) = CURRENT_DATE")
    long countTodaySearches();
    
    /**
     * 인기 검색어 조회 (상위 N개)
     * @param limit 상위 개수
     * @return List<String>
     */
    @Query("SELECT sl.searchKeyword FROM SearchLog sl " +
           "WHERE sl.searchKeyword IS NOT NULL " +
           "GROUP BY sl.searchKeyword ORDER BY COUNT(sl) DESC")
    List<String> findPopularSearchKeywords(Pageable pageable);
}
