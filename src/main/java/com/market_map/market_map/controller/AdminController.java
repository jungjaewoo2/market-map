package com.market_map.market_map.controller;

import com.market_map.market_map.entity.Store;
import com.market_map.market_map.entity.StoreImage;
import com.market_map.market_map.service.AdminService;
import com.market_map.market_map.service.StoreService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 관리자 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private StoreService storeService;
    
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
            .create();
    
    /**
     * 관리자 로그인
     * @param username 사용자명
     * @param password 비밀번호
     * @param request HTTP 요청
     * @return 로그인 결과 JSON
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request) {
        try {
            var admin = adminService.authenticate(username, password);
            
            if (admin != null) {
                // 세션에 관리자 정보 저장
                HttpSession session = request.getSession();
                session.setAttribute("adminId", admin.getAdminId());
                session.setAttribute("adminName", admin.getName());
                session.setAttribute("adminUsername", admin.getUsername());
                session.setMaxInactiveInterval(30 * 60); // 30분
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "로그인 성공");
                result.put("admin", Map.of(
                    "adminId", admin.getAdminId(),
                    "username", admin.getUsername(),
                    "name", admin.getName()
                ));
                
                return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(gson.toJson(result));
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
                return ResponseEntity.status(401)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(gson.toJson(error));
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "로그인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(gson.toJson(error));
        }
    }
    
    /**
     * 관리자 로그아웃
     * @param request HTTP 요청
     * @return 로그아웃 결과 JSON
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            Map<String, String> result = new HashMap<>();
            result.put("success", "true");
            result.put("message", "로그아웃되었습니다.");
            
            return ResponseEntity.ok(gson.toJson(result));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("success", "false");
            error.put("message", "로그아웃 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점 생성
     * @param store 상점 정보
     * @param images 업로드된 이미지 파일들
     * @param request HTTP 요청
     * @return 생성 결과 JSON
     */
    @PostMapping("/stores")
    public ResponseEntity<String> createStore(
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) Integer zoneNumber,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String businessHours,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer xCoordinate,
            @RequestParam(required = false) Integer yCoordinate,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            HttpServletRequest request) {
        try {
            logger.info("=== Store 생성 시작 ===");
            logger.info("입력 파라미터:");
            logger.info("  storeName: {}", storeName);
            logger.info("  storeCode: {}", storeCode);
            logger.info("  zoneNumber: {}", zoneNumber);
            logger.info("  phoneNumber: {}", phoneNumber);
            logger.info("  businessHours: {}", businessHours);
            logger.info("  description: {}", description);
            logger.info("  xCoordinate: {}", xCoordinate);
            logger.info("  yCoordinate: {}", yCoordinate);
            logger.info("  images: {}", images != null ? images.length + "개" : "없음");
            
            // 세션 확인 - Spring Security 방식으로 변경
            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.warn("세션 확인 실패 - 세션이 없음");
                Map<String, String> error = new HashMap<>();
                error.put("success", "false");
                error.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(gson.toJson(error));
            }
            
            // Spring Security의 인증 정보 확인
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                logger.warn("세션 확인 실패 - 인증되지 않은 사용자");
                Map<String, String> error = new HashMap<>();
                error.put("success", "false");
                error.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(gson.toJson(error));
            }
            
            logger.info("세션 확인 성공 - 사용자: {}", auth.getName());
            
            // Store 객체 생성
            Store store = new Store();
            store.setStoreName(storeName);
            store.setStoreCode(storeCode);
            store.setZoneNumber(zoneNumber);
            store.setPhoneNumber(phoneNumber);
            store.setBusinessHours(businessHours);
            store.setDescription(description);
            store.setXCoordinate(xCoordinate);
            store.setYCoordinate(yCoordinate);
            store.setIsActive(true);
            
            logger.info("Store 객체 생성 완료: {}", store);
            
            // 상점 생성
            logger.info("상점 생성 시도...");
            Store createdStore = storeService.createStore(store);
            logger.info("상점 생성 성공: {}", createdStore.getStoreId());
            
            // 이미지 업로드 처리
            if (images != null && images.length > 0) {
                logger.info("이미지 업로드 처리 시작: {}개", images.length);
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    if (!image.isEmpty()) {
                        String imageUrl = uploadImage(image, createdStore.getStoreId(), request);
                        if (imageUrl != null) {
                            StoreImage.ImageType imageType = (i == 0) ? 
                                StoreImage.ImageType.MAIN : StoreImage.ImageType.SUB;
                            storeService.addStoreImage(createdStore.getStoreId(), imageUrl, imageType);
                        }
                    }
                }
            }
            
            logger.info("=== Store 생성 완료 ===");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "상점이 성공적으로 등록되었습니다.");
            result.put("store", createdStore);
            
            return ResponseEntity.ok(gson.toJson(result));
        } catch (Exception e) {
            logger.error("=== Store 생성 오류 ===");
            logger.error("오류 메시지: {}", e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("success", "false");
            error.put("message", "상점 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점 수정
     * @param storeId 상점 ID
     * @param storeName 상점명
     * @param storeCode 상점 코드
     * @param zoneNumber 구역 번호
     * @param phoneNumber 전화번호
     * @param businessHours 영업시간
     * @param description 설명
     * @param xCoordinate X 좌표
     * @param yCoordinate Y 좌표
     * @param images 업로드된 이미지 파일들
     * @param request HTTP 요청
     * @return 수정 결과 JSON
     */
    @PutMapping("/stores/{storeId}")
    public ResponseEntity<String> updateStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) Integer zoneNumber,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String businessHours,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer xCoordinate,
            @RequestParam(required = false) Integer yCoordinate,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            HttpServletRequest request) {
        try {
            logger.info("=== Store 수정 시작 ===");
            logger.info("수정할 Store ID: {}", storeId);
            logger.info("입력 파라미터:");
            logger.info("  storeName: {}", storeName);
            logger.info("  storeCode: {}", storeCode);
            logger.info("  zoneNumber: {}", zoneNumber);
            logger.info("  phoneNumber: {}", phoneNumber);
            logger.info("  businessHours: {}", businessHours);
            logger.info("  description: {}", description);
            logger.info("  xCoordinate: {}", xCoordinate);
            logger.info("  yCoordinate: {}", yCoordinate);
            logger.info("  images: {}", images != null ? images.length + "개" : "없음");
            
            // 세션 확인 - Spring Security 방식으로 변경
            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.warn("세션 확인 실패 - 세션이 없음");
                Map<String, String> error = new HashMap<>();
                error.put("success", "false");
                error.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(gson.toJson(error));
            }
            
            // Spring Security의 인증 정보 확인
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                logger.warn("세션 확인 실패 - 인증되지 않은 사용자");
                Map<String, String> error = new HashMap<>();
                error.put("success", "false");
                error.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(gson.toJson(error));
            }
            
            logger.info("세션 확인 성공 - 사용자: {}", auth.getName());
            
            // 기존 Store 조회
            Store existingStore = storeService.getStoreById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상점입니다."));
            
            // Store 객체 업데이트
            existingStore.setStoreName(storeName);
            existingStore.setStoreCode(storeCode);
            existingStore.setZoneNumber(zoneNumber);
            existingStore.setPhoneNumber(phoneNumber);
            existingStore.setBusinessHours(businessHours);
            existingStore.setDescription(description);
            existingStore.setXCoordinate(xCoordinate);
            existingStore.setYCoordinate(yCoordinate);
            existingStore.setIsActive(true);
            
            logger.info("Store 객체 업데이트 완료: {}", existingStore);
            
            // 상점 수정
            logger.info("상점 수정 시도...");
            Store updatedStore = storeService.updateStore(existingStore);
            logger.info("상점 수정 성공: {}", updatedStore.getStoreId());
            
            // 이미지 업로드 처리
            if (images != null && images.length > 0) {
                logger.info("이미지 업로드 처리 시작: {}개", images.length);
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    if (!image.isEmpty()) {
                        String imageUrl = uploadImage(image, storeId, request);
                        if (imageUrl != null) {
                            StoreImage.ImageType imageType = (i == 0) ? 
                                StoreImage.ImageType.MAIN : StoreImage.ImageType.SUB;
                            storeService.addStoreImage(storeId, imageUrl, imageType);
                        }
                    }
                }
            }
            
            logger.info("=== Store 수정 완료 ===");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "상점 정보가 성공적으로 수정되었습니다.");
            result.put("store", updatedStore);
            
            return ResponseEntity.ok(gson.toJson(result));
        } catch (Exception e) {
            logger.error("=== Store 수정 오류 ===");
            logger.error("오류 메시지: {}", e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("success", "false");
            error.put("message", "상점 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점 삭제
     * @param storeId 상점 ID
     * @param request HTTP 요청
     * @return 삭제 결과 JSON
     */
    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<String> deleteStore(@PathVariable Long storeId, HttpServletRequest request) {
        try {
            // 세션 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("adminId") == null) {
                Map<String, String> error = new HashMap<>();
                error.put("success", "false");
                error.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(gson.toJson(error));
            }
            
            boolean deleted = storeService.deleteStore(storeId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", deleted);
            result.put("message", deleted ? "상점이 삭제되었습니다." : "상점 삭제에 실패했습니다.");
            
            return ResponseEntity.ok(gson.toJson(result));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("success", "false");
            error.put("message", "상점 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 상점 이미지 삭제
     * @param imageId 이미지 ID
     * @param request HTTP 요청
     * @return 삭제 결과 JSON
     */
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<String> deleteStoreImage(@PathVariable Long imageId, HttpServletRequest request) {
        try {
            // 세션 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("adminId") == null) {
                Map<String, String> error = new HashMap<>();
                error.put("success", "false");
                error.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(gson.toJson(error));
            }
            
            boolean deleted = storeService.deleteStoreImage(imageId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", deleted);
            result.put("message", deleted ? "이미지가 삭제되었습니다." : "이미지 삭제에 실패했습니다.");
            
            return ResponseEntity.ok(gson.toJson(result));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("success", "false");
            error.put("message", "이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(gson.toJson(error));
        }
    }
    
    /**
     * 비밀번호 변경
     * @param oldPassword 기존 비밀번호
     * @param newPassword 새 비밀번호
     * @param request HTTP 요청
     * @return 변경 결과 JSON
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            HttpServletRequest request) {
        logger.info("=== 비밀번호 변경 요청 시작 ===");
        logger.info("요청 URI: {}", request.getRequestURI());
        logger.info("요청 메서드: {}", request.getMethod());
        logger.info("Content-Type: {}", request.getContentType());
        
        try {
            // Spring Security 인증 확인
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            logger.info("Spring Security 인증: {}", auth != null ? "인증됨" : "인증 안됨");
            
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                logger.warn("Spring Security 인증 실패");
                Map<String, String> error = new HashMap<>();
                error.put("success", "false");
                error.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(gson.toJson(error));
            }
            
            // 사용자명으로 관리자 정보 조회
            String username = auth.getName();
            logger.info("인증된 사용자명: {}", username);
            
            var adminOpt = adminService.getAdminByUsername(username);
            if (!adminOpt.isPresent()) {
                logger.warn("관리자 정보를 찾을 수 없음: {}", username);
                Map<String, String> error = new HashMap<>();
                error.put("success", "false");
                error.put("message", "관리자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(401)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(gson.toJson(error));
            }
            
            Long adminId = adminOpt.get().getAdminId();
            logger.info("비밀번호 변경 시도 - adminId: {}", adminId);
            logger.info("oldPassword 존재: {}, newPassword 존재: {}", 
                oldPassword != null && !oldPassword.isEmpty(), 
                newPassword != null && !newPassword.isEmpty());
            
            boolean changed = adminService.changePassword(adminId, oldPassword, newPassword);
            
            logger.info("비밀번호 변경 결과: {}", changed);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", changed);
            result.put("message", changed ? "비밀번호가 변경되었습니다." : "비밀번호 변경에 실패했습니다.");
            
            String jsonResponse = gson.toJson(result);
            logger.info("응답 JSON: {}", jsonResponse);
            logger.info("=== 비밀번호 변경 요청 완료 ===");
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(jsonResponse);
        } catch (Exception e) {
            logger.error("=== 비밀번호 변경 중 오류 발생 ===");
            logger.error("오류 메시지: {}", e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("success", "false");
            error.put("message", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(gson.toJson(error));
        }
    }
    
    /**
     * 이미지 업로드 처리
     * @param image 업로드할 이미지 파일
     * @param storeId 상점 ID
     * @param request HTTP 요청
     * @return 업로드된 이미지 URL
     */
    private String uploadImage(MultipartFile image, Long storeId, HttpServletRequest request) {
        try {
            // 업로드 디렉토리 생성 (/uploads/stores)
            String uploadDir = request.getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "stores";
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }
            
            // 파일명 생성
            String originalFilename = image.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "store_" + storeId + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            
            // 파일 저장
            Path filePath = Paths.get(uploadPath.getAbsolutePath(), fileName);
            Files.copy(image.getInputStream(), filePath);
            
            // 웹 경로 반환 (/static/uploads/stores/)
            return "/static/uploads/stores/" + fileName;
        } catch (IOException e) {
            logger.error("이미지 업로드 오류: {}", e.getMessage(), e);
            return null;
        }
    }
}
