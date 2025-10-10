package com.market_map.market_map.repository;

import com.market_map.market_map.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    /**
     * 활성 상점 목록 조회
     * @return List<Store>
     */
    List<Store> findByIsActiveTrueOrderByZoneNumberAscStoreCodeAsc();
    
    /**
     * 구역별 활성 상점 목록 조회
     * @param zoneNumber 구역 번호
     * @return List<Store>
     */
    List<Store> findByZoneNumberAndIsActiveTrueOrderByStoreCodeAsc(Integer zoneNumber);
    
    /**
     * 상점명으로 검색 (부분 일치)
     * @param storeName 상점명
     * @return List<Store>
     */
    @Query("SELECT s FROM Store s WHERE s.storeName LIKE %:storeName% AND s.isActive = true ORDER BY s.zoneNumber, s.storeCode")
    List<Store> findByStoreNameContainingAndIsActiveTrue(@Param("storeName") String storeName);
    
    /**
     * 상점 코드로 검색
     * @param storeCode 상점 코드
     * @return Optional<Store>
     */
    Optional<Store> findByStoreCodeAndIsActiveTrue(String storeCode);
    
    /**
     * 좌표 범위 내 상점 검색
     * @param x X 좌표
     * @param y Y 좌표
     * @param radius 반경
     * @return List<Store>
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND " +
           "ABS(s.xCoordinate - :x) <= :radius AND ABS(s.yCoordinate - :y) <= :radius " +
           "ORDER BY (POWER(s.xCoordinate - :x, 2) + POWER(s.yCoordinate - :y, 2))")
    List<Store> findStoresByCoordinatesWithinRadius(@Param("x") Integer x, @Param("y") Integer y, @Param("radius") Integer radius);
    
    /**
     * 좌표로 가장 가까운 상점 검색
     * @param x X 좌표
     * @param y Y 좌표
     * @return Optional<Store>
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true ORDER BY " +
           "(POWER(s.xCoordinate - :x, 2) + POWER(s.yCoordinate - :y, 2)) ASC")
    Optional<Store> findNearestStoreByCoordinates(@Param("x") Integer x, @Param("y") Integer y);
    
    /**
     * 전체 텍스트 검색 (상점명, 설명)
     * @param keyword 검색 키워드
     * @return List<Store>
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND " +
           "(s.storeName LIKE %:keyword% OR s.storeCode LIKE %:keyword% OR s.description LIKE %:keyword%) " +
           "ORDER BY s.zoneNumber, s.storeCode")
    List<Store> searchStoresByKeyword(@Param("keyword") String keyword);
    
    /**
     * 전화번호로 상점 검색
     * @param phoneNumber 전화번호
     * @return List<Store>
     */
    List<Store> findByPhoneNumberContainingAndIsActiveTrue(String phoneNumber);
    
    /**
     * 카테고리별 상점 조회 (향후 확장용 - 현재는 비활성화)
     * @param categoryId 카테고리 ID
     * @return List<Store>
     */
    // @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.categoryId = :categoryId ORDER BY s.zoneNumber, s.storeCode")
    // List<Store> findByCategoryIdAndIsActiveTrue(@Param("categoryId") Integer categoryId);
    
    /**
     * 구역별 상점 수 조회
     * @param zoneNumber 구역 번호
     * @return long
     */
    long countByZoneNumberAndIsActiveTrue(Integer zoneNumber);
    
    /**
     * 전체 활성 상점 수 조회
     * @return long
     */
    long countByIsActiveTrue();
    
    /**
     * 페이지네이션을 지원하는 상점 목록 조회
     * @param pageable 페이지 정보
     * @return Page<Store>
     */
    Page<Store> findByIsActiveTrueOrderByZoneNumberAscStoreCodeAsc(Pageable pageable);
    
    /**
     * 관리자가 생성한 상점 목록 조회
     * @param createdById 관리자 ID
     * @return List<Store>
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.createdBy.adminId = :createdById ORDER BY s.createdAt DESC")
    List<Store> findByCreatedByAdminIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("createdById") Long createdById);
}
