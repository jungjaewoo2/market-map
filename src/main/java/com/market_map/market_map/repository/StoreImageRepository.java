package com.market_map.market_map.repository;

import com.market_map.market_map.entity.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {
    
    /**
     * 상점별 이미지 목록 조회 (표시 순서대로)
     * @param storeId 상점 ID
     * @return List<StoreImage>
     */
    List<StoreImage> findByStoreStoreIdOrderByDisplayOrderAsc(Long storeId);
    
    /**
     * 활성 상점 이미지 목록 조회
     * @param storeId 상점 ID
     * @return List<StoreImage>
     */
    List<StoreImage> findByStoreStoreIdAndIsActiveTrue(Long storeId);
    
    /**
     * 상점 이미지 목록 조회 (is_active가 true이거나 null인 경우 포함)
     * @param storeId 상점 ID
     * @return List<StoreImage>
     */
    @Query("SELECT si FROM StoreImage si WHERE si.store.storeId = :storeId AND (si.isActive = true OR si.isActive IS NULL)")
    List<StoreImage> findByStoreStoreIdAndIsActiveTrueOrNull(@Param("storeId") Long storeId);
    
    /**
     * 상점 이미지 목록 조회 (Store 관계 제외하고 필요한 필드만 조회)
     * @param storeId 상점 ID
     * @return List<Map<String, Object>>
     */
    @Query("SELECT new map(si.imageId as imageId, si.imageUrl as imageUrl, si.imageType as imageType, si.displayOrder as displayOrder, si.isActive as isActive, si.uploadedAt as uploadedAt) FROM StoreImage si WHERE si.store.storeId = :storeId AND (si.isActive = true OR si.isActive IS NULL)")
    List<Map<String, Object>> findImageDataByStoreId(@Param("storeId") Long storeId);
    
    /**
     * 상점의 대표 이미지 조회
     * @param storeId 상점 ID
     * @return List<StoreImage>
     */
    @Query("SELECT si FROM StoreImage si WHERE si.store.storeId = :storeId AND si.imageType = 'MAIN'")
    List<StoreImage> findMainImagesByStoreId(@Param("storeId") Long storeId);
    
    /**
     * 상점의 서브 이미지 목록 조회
     * @param storeId 상점 ID
     * @return List<StoreImage>
     */
    @Query("SELECT si FROM StoreImage si WHERE si.store.storeId = :storeId AND si.imageType = 'SUB' ORDER BY si.displayOrder ASC")
    List<StoreImage> findSubImagesByStoreId(@Param("storeId") Long storeId);
    
    /**
     * 상점의 이미지 수 조회
     * @param storeId 상점 ID
     * @return long
     */
    long countByStoreStoreId(Long storeId);
    
    /**
     * 상점의 특정 타입 이미지 수 조회
     * @param storeId 상점 ID
     * @param imageType 이미지 타입
     * @return long
     */
    long countByStoreStoreIdAndImageType(Long storeId, StoreImage.ImageType imageType);
    
    /**
     * 상점별 이미지 삭제
     * @param storeId 상점 ID
     */
    void deleteByStoreStoreId(Long storeId);
    
    /**
     * 특정 이미지 타입의 이미지 삭제
     * @param storeId 상점 ID
     * @param imageType 이미지 타입
     */
    void deleteByStoreStoreIdAndImageType(Long storeId, StoreImage.ImageType imageType);
}
