package com.market_map.market_map.controller;

import com.market_map.market_map.entity.Store;
import com.market_map.market_map.service.StoreService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 상점 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {
    
    @Autowired
    private StoreService storeService;
    
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
            .create();
    
    /**
     * 전체 상점 목록 조회
     * @param zone 구역 번호 (선택사항)
     * @return 상점 목록 JSON
     */
    @GetMapping(value = {"", "/", "/list"})
    public ResponseEntity<String> getAllStores(@RequestParam(required = false) Integer zone) {
        try {
            List<Store> stores;
            
            if (zone != null) {
                stores = storeService.getStoresByZone(zone);
            } else {
                stores = storeService.getAllStores();
            }
            
            return ResponseEntity.ok(gson.toJson(stores));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "상점 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점 ID로 조회
     * @param id 상점 ID
     * @return 상점 정보 JSON
     */
    @GetMapping("/{id}")
    public ResponseEntity<String> getStoreById(@PathVariable Long id) {
        try {
            Optional<Store> storeOpt = storeService.getStoreById(id);
            
            if (storeOpt.isPresent()) {
                return ResponseEntity.ok(gson.toJson(storeOpt.get()));
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "상점을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(gson.toJson(error));
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "상점 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점 코드로 조회
     * @param code 상점 코드
     * @return 상점 정보 JSON
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<String> getStoreByCode(@PathVariable String code) {
        try {
            Optional<Store> storeOpt = storeService.getStoreByCode(code);
            
            if (storeOpt.isPresent()) {
                return ResponseEntity.ok(gson.toJson(storeOpt.get()));
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "상점을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(gson.toJson(error));
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "상점 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 좌표로 상점 검색
     * @param x X 좌표
     * @param y Y 좌표
     * @param radius 반경 (기본값: 50)
     * @return 상점 목록 JSON
     */
    @GetMapping("/search/location")
    public ResponseEntity<String> searchStoresByLocation(
            @RequestParam Integer x, 
            @RequestParam Integer y,
            @RequestParam(defaultValue = "50") Integer radius) {
        try {
            List<Store> stores = storeService.getStoresByCoordinates(x, y, radius);
            return ResponseEntity.ok(gson.toJson(stores));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "위치 기반 검색 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 가장 가까운 상점 검색
     * @param x X 좌표
     * @param y Y 좌표
     * @return 가장 가까운 상점 JSON
     */
    @GetMapping("/search/nearest")
    public ResponseEntity<String> getNearestStore(@RequestParam Integer x, @RequestParam Integer y) {
        try {
            Optional<Store> storeOpt = storeService.getNearestStore(x, y);
            
            if (storeOpt.isPresent()) {
                return ResponseEntity.ok(gson.toJson(storeOpt.get()));
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "근처에 상점을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(gson.toJson(error));
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "가장 가까운 상점 검색 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 키워드로 상점 검색
     * @param keyword 검색 키워드
     * @return 상점 목록 JSON
     */
    @GetMapping("/search")
    public ResponseEntity<String> searchStores(@RequestParam String keyword) {
        try {
            List<Store> stores = storeService.searchStores(keyword);
            return ResponseEntity.ok(gson.toJson(stores));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "검색 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점명으로 검색
     * @param name 상점명
     * @return 상점 목록 JSON
     */
    @GetMapping("/search/name")
    public ResponseEntity<String> searchStoresByName(@RequestParam String name) {
        try {
            List<Store> stores = storeService.searchStoresByName(name);
            return ResponseEntity.ok(gson.toJson(stores));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "상점명 검색 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 전화번호로 검색
     * @param phone 전화번호
     * @return 상점 목록 JSON
     */
    @GetMapping("/search/phone")
    public ResponseEntity<String> searchStoresByPhone(@RequestParam String phone) {
        try {
            List<Store> stores = storeService.searchStoresByPhone(phone);
            return ResponseEntity.ok(gson.toJson(stores));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "전화번호 검색 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점 이미지 조회
     * @param storeId 상점 ID
     * @return 상점 이미지 목록 JSON
     */
    @GetMapping("/{storeId}/images")
    public ResponseEntity<String> getStoreImages(@PathVariable Long storeId) {
        try {
            List<Map<String, Object>> imageData = storeService.getStoreImageData(storeId);
            return ResponseEntity.ok(gson.toJson(imageData));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "상점 이미지 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점 통계 정보 조회
     * @return 통계 정보 JSON
     */
    @GetMapping("/stats")
    public ResponseEntity<String> getStoreStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStores", storeService.countActiveStores());
            stats.put("zone1Stores", storeService.countStoresByZone(1));
            stats.put("zone2Stores", storeService.countStoresByZone(2));
            stats.put("zone3Stores", storeService.countStoresByZone(3));
            stats.put("zone4Stores", storeService.countStoresByZone(4));
            stats.put("zone5Stores", storeService.countStoresByZone(5));
            
            return ResponseEntity.ok(gson.toJson(stats));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "통계 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
}
