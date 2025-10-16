package com.market_map.market_map.service;

import com.market_map.market_map.entity.Store;
import com.market_map.market_map.entity.StoreImage;
import com.market_map.market_map.repository.StoreRepository;
import com.market_map.market_map.repository.StoreImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 상점 관련 비즈니스 로직 서비스
 */
@Service
@Transactional
public class StoreService {
    
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private StoreImageRepository storeImageRepository;
    
    /**
     * 모든 활성 상점 목록 조회
     * @return 상점 목록
     */
    @Transactional(readOnly = true)
    public List<Store> getAllStores() {
        return storeRepository.findByIsActiveTrueOrderByZoneNumberAscStoreCodeAsc();
    }
    
    /**
     * 구역별 상점 목록 조회
     * @param zoneNumber 구역 번호
     * @return 상점 목록
     */
    @Transactional(readOnly = true)
    public List<Store> getStoresByZone(Integer zoneNumber) {
        return storeRepository.findByZoneNumberAndIsActiveTrueOrderByStoreCodeAsc(zoneNumber);
    }
    
    /**
     * 상점 ID로 조회
     * @param storeId 상점 ID
     * @return 상점 정보
     */
    @Transactional(readOnly = true)
    public Optional<Store> getStoreById(Long storeId) {
        return storeRepository.findById(storeId);
    }
    
    /**
     * 상점 코드로 조회
     * @param storeCode 상점 코드
     * @return 상점 정보
     */
    @Transactional(readOnly = true)
    public Optional<Store> getStoreByCode(String storeCode) {
        return storeRepository.findByStoreCodeAndIsActiveTrue(storeCode);
    }
    
    /**
     * 좌표 범위 내 상점 검색
     * @param x X 좌표
     * @param y Y 좌표
     * @param radius 반경
     * @return 상점 목록
     */
    @Transactional(readOnly = true)
    public List<Store> getStoresByCoordinates(Integer x, Integer y, Integer radius) {
        return storeRepository.findStoresByCoordinatesWithinRadius(x, y, radius);
    }
    
    /**
     * 가장 가까운 상점 검색
     * @param x X 좌표
     * @param y Y 좌표
     * @return 가장 가까운 상점
     */
    @Transactional(readOnly = true)
    public Optional<Store> getNearestStore(Integer x, Integer y) {
        return storeRepository.findNearestStoreByCoordinates(x, y);
    }
    
    /**
     * 키워드로 상점 검색
     * @param keyword 검색 키워드
     * @return 상점 목록
     */
    @Transactional(readOnly = true)
    public List<Store> searchStores(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStores();
        }
        return storeRepository.searchStoresByKeyword(keyword.trim());
    }
    
    /**
     * 상점명으로 검색
     * @param storeName 상점명
     * @return 상점 목록
     */
    @Transactional(readOnly = true)
    public List<Store> searchStoresByName(String storeName) {
        return storeRepository.findByStoreNameContainingAndIsActiveTrue(storeName);
    }
    
    /**
     * 전화번호로 검색
     * @param phoneNumber 전화번호
     * @return 상점 목록
     */
    @Transactional(readOnly = true)
    public List<Store> searchStoresByPhone(String phoneNumber) {
        return storeRepository.findByPhoneNumberContainingAndIsActiveTrue(phoneNumber);
    }
    
    /**
     * 상점 생성
     * @param store 상점 정보
     * @return 생성된 상점 정보
     */
    public Store createStore(Store store) {
        logger.info("=== StoreService.createStore 시작 ===");
        logger.info("입력된 Store: {}", store);
        
        // 유효성 검사
        logger.info("유효성 검사 시작...");
        validateStore(store);
        logger.info("유효성 검사 완료");
        
        // 상점 코드 중복 검사
        if (store.getStoreCode() != null && 
            storeRepository.findByStoreCodeAndIsActiveTrue(store.getStoreCode()).isPresent()) {
            logger.warn("상점 코드 중복 오류: {}", store.getStoreCode());
            throw new IllegalArgumentException("이미 존재하는 상점 코드입니다.");
        }
        
        logger.info("데이터베이스 저장 시도...");
        Store savedStore = storeRepository.save(store);
        logger.info("데이터베이스 저장 완료: {}", savedStore.getStoreId());
        
        return savedStore;
    }
    
    /**
     * 상점 수정
     * @param store 수정할 상점 정보
     * @return 수정된 상점 정보
     */
    public Store updateStore(Store store) {
        Store existingStore = storeRepository.findById(store.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상점입니다."));
        
        // 유효성 검사
        validateStore(store);
        
        // 상점 코드 중복 검사 (자기 자신 제외)
        if (store.getStoreCode() != null && 
            !store.getStoreCode().equals(existingStore.getStoreCode()) &&
            storeRepository.findByStoreCodeAndIsActiveTrue(store.getStoreCode()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 상점 코드입니다.");
        }
        
        // 정보 업데이트
        existingStore.setStoreName(store.getStoreName());
        existingStore.setStoreCode(store.getStoreCode());
        existingStore.setZoneNumber(store.getZoneNumber());
        existingStore.setPhoneNumber(store.getPhoneNumber());
        existingStore.setAddress(store.getAddress());
        existingStore.setDetailAddress(store.getDetailAddress());
        existingStore.setXCoordinate(store.getXCoordinate());
        existingStore.setYCoordinate(store.getYCoordinate());
        existingStore.setMarkerRadius(store.getMarkerRadius());
        existingStore.setBusinessHours(store.getBusinessHours());
        existingStore.setDescription(store.getDescription());
        
        return storeRepository.save(existingStore);
    }
    
    /**
     * 상점 삭제 (논리 삭제)
     * @param storeId 상점 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteStore(Long storeId) {
        logger.info("상점 삭제 처리 시작 - storeId: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상점입니다."));
        
        logger.info("삭제 전 상점 정보 - storeName: {}, isActive: {}", store.getStoreName(), store.getIsActive());
        
        store.setIsActive(false);
        Store savedStore = storeRepository.save(store);
        
        logger.info("삭제 후 상점 정보 - storeName: {}, isActive: {}", savedStore.getStoreName(), savedStore.getIsActive());
        logger.info("상점 삭제 처리 완료 - storeId: {}", storeId);
        
        return true;
    }
    
    /**
     * 상점 물리 삭제
     * @param storeId 상점 ID
     * @return 삭제 성공 여부
     */
    public boolean permanentlyDeleteStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new IllegalArgumentException("존재하지 않는 상점입니다.");
        }
        
        // 관련 이미지도 함께 삭제
        storeImageRepository.deleteByStoreStoreId(storeId);
        storeRepository.deleteById(storeId);
        
        return true;
    }
    
    /**
     * 상점 이미지 추가
     * @param storeId 상점 ID
     * @param imageUrl 이미지 URL
     * @param imageType 이미지 타입
     * @return 생성된 이미지 정보
     */
    public StoreImage addStoreImage(Long storeId, String imageUrl, StoreImage.ImageType imageType) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상점입니다."));
        
        StoreImage image = new StoreImage(store, imageUrl, imageType);
        return storeImageRepository.save(image);
    }
    
    /**
     * 상점 이미지 삭제
     * @param imageId 이미지 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteStoreImage(Long imageId) {
        if (!storeImageRepository.existsById(imageId)) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다.");
        }
        
        storeImageRepository.deleteById(imageId);
        return true;
    }
    
    /**
     * 상점별 이미지 목록 조회
     * @param storeId 상점 ID
     * @return 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<StoreImage> getStoreImages(Long storeId) {
        return storeImageRepository.findByStoreStoreIdAndIsActiveTrueOrNull(storeId);
    }
    
    /**
     * 상점별 이미지 데이터 조회 (직렬화 문제 해결)
     * @param storeId 상점 ID
     * @return 이미지 데이터 목록
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStoreImageData(Long storeId) {
        return storeImageRepository.findImageDataByStoreId(storeId);
    }
    
    /**
     * 페이지네이션을 지원하는 상점 목록 조회
     * @param pageable 페이지 정보
     * @return 페이지된 상점 목록
     */
    @Transactional(readOnly = true)
    public Page<Store> getStoresWithPagination(Pageable pageable) {
        return storeRepository.findByIsActiveTrueOrderByZoneNumberAscStoreCodeAsc(pageable);
    }
    
    /**
     * 구역별 상점 수 조회
     * @param zoneNumber 구역 번호
     * @return 상점 수
     */
    @Transactional(readOnly = true)
    public long countStoresByZone(Integer zoneNumber) {
        return storeRepository.countByZoneNumberAndIsActiveTrue(zoneNumber);
    }
    
    /**
     * 전체 활성 상점 수 조회
     * @return 상점 수
     */
    @Transactional(readOnly = true)
    public long countActiveStores() {
        return storeRepository.countByIsActiveTrue();
    }
    
    /**
     * 상점 정보 유효성 검사
     * @param store 상점 정보
     * @throws IllegalArgumentException 유효하지 않은 경우
     */
    private void validateStore(Store store) {
        if (store.getStoreName() == null || store.getStoreName().trim().isEmpty()) {
            throw new IllegalArgumentException("상점명은 필수 입력 항목입니다.");
        }
        
        if (store.getXCoordinate() == null || store.getXCoordinate() <= 0) {
            throw new IllegalArgumentException("유효한 X 좌표를 설정해주세요.");
        }
        
        if (store.getYCoordinate() == null || store.getYCoordinate() <= 0) {
            throw new IllegalArgumentException("유효한 Y 좌표를 설정해주세요.");
        }
        
        if (store.getZoneNumber() != null && (store.getZoneNumber() < 1 || store.getZoneNumber() > 5)) {
            throw new IllegalArgumentException("유효한 구역 번호를 선택해주세요. (1-5)");
        }
        
        if (store.getStoreName().length() > 100) {
            throw new IllegalArgumentException("상점명은 100자를 초과할 수 없습니다.");
        }
        
        if (store.getStoreCode() != null && store.getStoreCode().length() > 50) {
            throw new IllegalArgumentException("상점 코드는 50자를 초과할 수 없습니다.");
        }
    }
}
