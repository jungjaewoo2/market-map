package com.market_map.market_map.repository;

import com.market_map.market_map.entity.MarketMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketMapRepository extends JpaRepository<MarketMap, Long> {
    
    /**
     * 활성 지도 목록 조회
     * @return List<MarketMap>
     */
    List<MarketMap> findByIsActiveTrueOrderByCreatedAtAsc();
    
    /**
     * 활성 지도 조회 (단일)
     * @return Optional<MarketMap>
     */
    @Query("SELECT m FROM MarketMap m WHERE m.isActive = true ORDER BY m.createdAt ASC")
    Optional<MarketMap> findActiveMap();
    
    /**
     * 지도명으로 검색
     * @param mapName 지도명
     * @return List<MarketMap>
     */
    List<MarketMap> findByMapNameContainingIgnoreCase(String mapName);
    
    /**
     * 활성 지도 수 조회
     * @return long
     */
    long countByIsActiveTrue();
}
