// ========== Model 클래스 (수정) ==========
// Store.java - 이미지 좌표 기반
package com.storeMap.model;

import java.sql.Timestamp;
import java.util.List;

public class Store {
    private int storeId;
    private String storeName;
    private String storeCode;  // 상점 코드 (예: 1-A01)
    private int zoneNumber;    // 구역 번호
    private int categoryId;
    private String categoryName;
    private String phoneNumber;
    private String address;
    private String detailAddress;
    private int mapId;         // 지도 이미지 참조
    private int xCoordinate;   // 이미지 X 좌표
    private int yCoordinate;   // 이미지 Y 좌표
    private int markerRadius;  // 마커 반경
    private String businessHours;
    private String description;
    private boolean isActive;
    private List<StoreImage> images;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Getters and Setters
    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }
    
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    
    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }
    
    public int getZoneNumber() { return zoneNumber; }
    public void setZoneNumber(int zoneNumber) { this.zoneNumber = zoneNumber; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public int getMapId() { return mapId; }
    public void setMapId(int mapId) { this.mapId = mapId; }
    
    public int getXCoordinate() { return xCoordinate; }
    public void setXCoordinate(int xCoordinate) { this.xCoordinate = xCoordinate; }
    
    public int getYCoordinate() { return yCoordinate; }
    public void setYCoordinate(int yCoordinate) { this.yCoordinate = yCoordinate; }
    
    public int getMarkerRadius() { return markerRadius; }
    public void setMarkerRadius(int markerRadius) { this.markerRadius = markerRadius; }
    
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public List<StoreImage> getImages() { return images; }
    public void setImages(List<StoreImage> images) { this.images = images; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}

// ========== DAO 클래스 (수정) ==========
// StoreDAO.java - 이미지 좌표 기반
package com.storeMap.dao;

import com.storeMap.config.DatabaseConfig;
import com.storeMap.model.Store;
import com.storeMap.model.StoreImage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreDAO {
    
    public List<Store> getAllStores() throws SQLException {
        String sql = "SELECT s.*, c.category_name FROM stores s " +
                    "LEFT JOIN store_categories c ON s.category_id = c.category_id " +
                    "WHERE s.is_active = true ORDER BY s.zone_number, s.store_code";
        
        List<Store> stores = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                stores.add(mapResultSetToStore(rs));
            }
        }
        return stores;
    }
    
    public List<Store> getStoresByZone(int zoneNumber) throws SQLException {
        String sql = "SELECT s.*, c.category_name FROM stores s " +
                    "LEFT JOIN store_categories c ON s.category_id = c.category_id " +
                    "WHERE s.is_active = true AND s.zone_number = ? " +
                    "ORDER BY s.store_code";
        
        List<Store> stores = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, zoneNumber);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stores.add(mapResultSetToStore(rs));
                }
            }
        }
        return stores;
    }
    
    public Store getStoreById(int storeId) throws SQLException {
        String sql = "SELECT s.*, c.category_name FROM stores s " +
                    "LEFT JOIN store_categories c ON s.category_id = c.category_id " +
                    "WHERE s.store_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, storeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Store store = mapResultSetToStore(rs);
                    store.setImages(getStoreImages(storeId));
                    return store;
                }
            }
        }
        return null;
    }
    
    public Store getStoreByCoordinates(int x, int y, int radius) throws SQLException {
        String sql = "SELECT s.*, c.category_name FROM stores s " +
                    "LEFT JOIN store_categories c ON s.category_id = c.category_id " +
                    "WHERE s.is_active = true AND " +
                    "ABS(s.x_coordinate - ?) <= ? AND ABS(s.y_coordinate - ?) <= ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, x);
            pstmt.setInt(2, radius);
            pstmt.setInt(3, y);
            pstmt.setInt(4, radius);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStore(rs);
                }
            }
        }
        return null;
    }
    
    public List<Store> searchStores(String keyword) throws SQLException {
        String sql = "SELECT s.*, c.category_name FROM stores s " +
                    "LEFT JOIN store_categories c ON s.category_id = c.category_id " +
                    "WHERE s.is_active = true AND " +
                    "(s.store_name LIKE ? OR s.store_code LIKE ? OR s.description LIKE ?) " +
                    "ORDER BY s.zone_number, s.store_code";
        
        List<Store> stores = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stores.add(mapResultSetToStore(rs));
                }
            }
        }
        
        // 검색 로그 저장
        logSearch(keyword, stores.size());
        
        return stores;
    }
    
    public int insertStore(Store store) throws SQLException {
        String sql = "INSERT INTO stores (store_name, store_code, zone_number, category_id, " +
                    "phone_number, address, detail_address, map_id, x_coordinate, y_coordinate, " +
                    "marker_radius, business_hours, description, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, 
                                        Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, store.getStoreName());
            pstmt.setString(2, store.getStoreCode());
            pstmt.setInt(3, store.getZoneNumber());
            pstmt.setInt(4, store.getCategoryId());
            pstmt.setString(5, store.getPhoneNumber());
            pstmt.setString(6, store.getAddress());
            pstmt.setString(7, store.getDetailAddress());
            pstmt.setInt(8, store.getMapId() > 0 ? store.getMapId() : 1); // 기본 지도 ID
            pstmt.setInt(9, store.getXCoordinate());
            pstmt.setInt(10, store.getYCoordinate());
            pstmt.setInt(11, store.getMarkerRadius() > 0 ? store.getMarkerRadius() : 10);
            pstmt.setString(12, store.getBusinessHours());
            pstmt.setString(13, store.getDescription());
            pstmt.setInt(14, 1); // TODO: 실제 로그인한 관리자 ID 사용
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }
    
    public boolean updateStore(Store store) throws SQLException {
        String sql = "UPDATE stores SET store_name = ?, store_code = ?, zone_number = ?, " +
                    "category_id = ?, phone_number = ?, address = ?, detail_address = ?, " +
                    "x_coordinate = ?, y_coordinate = ?, business_hours = ?, " +
                    "description = ? WHERE store_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, store.getStoreName());
            pstmt.setString(2, store.getStoreCode());
            pstmt.setInt(3, store.getZoneNumber());
            pstmt.setInt(4, store.getCategoryId());
            pstmt.setString(5, store.getPhoneNumber());
            pstmt.setString(6, store.getAddress());
            pstmt.setString(7, store.getDetailAddress());
            pstmt.setInt(8, store.getXCoordinate());
            pstmt.setInt(9, store.getYCoordinate());
            pstmt.setString(10, store.getBusinessHours());
            pstmt.setString(11, store.getDescription());
            pstmt.setInt(12, store.getStoreId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    private Store mapResultSetToStore(ResultSet rs) throws SQLException {
        Store store = new Store();
        store.setStoreId(rs.getInt("store_id"));
        store.setStoreName(rs.getString("store_name"));
        store.setStoreCode(rs.getString("store_code"));
        store.setZoneNumber(rs.getInt("zone_number"));
        store.setCategoryId(rs.getInt("category_id"));
        store.setCategoryName(rs.getString("category_name"));
        store.setPhoneNumber(rs.getString("phone_number"));
        store.setAddress(rs.getString("address"));
        store.setDetailAddress(rs.getString("detail_address"));
        store.setMapId(rs.getInt("map_id"));
        store.setXCoordinate(rs.getInt("x_coordinate"));
        store.setYCoordinate(rs.getInt("y_coordinate"));
        store.setMarkerRadius(rs.getInt("marker_radius"));
        store.setBusinessHours(rs.getString("business_hours"));
        store.setDescription(rs.getString("description"));
        store.setActive(rs.getBoolean("is_active"));
        store.setCreatedAt(rs.getTimestamp("created_at"));
        store.setUpdatedAt(rs.getTimestamp("updated_at"));
        return store;
    }
    
    private List<StoreImage> getStoreImages(int storeId) throws SQLException {
        String sql = "SELECT * FROM store_images WHERE store_id = ? ORDER BY display_order";
        List<StoreImage> images = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, storeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StoreImage image = new StoreImage();
                    image.setImageId(rs.getInt("image_id"));
                    image.setStoreId(rs.getInt("store_id"));
                    image.setImageUrl(rs.getString("image_url"));
                    image.setImageType(rs.getString("image_type"));
                    image.setDisplayOrder(rs.getInt("display_order"));
                    images.add(image);
                }
            }
        }
        return images;
    }
    
    private void logSearch(String keyword, int resultCount) {
        String sql = "INSERT INTO search_logs (search_keyword, search_type, result_count) " +
                    "VALUES (?, 'name', ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, keyword);
            pstmt.setInt(2, resultCount);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            // 로그 저장 실패는 무시
            e.printStackTrace();
        }
    }
}

// ========== Controller 클래스 ==========
// StoreController.java - REST API 컨트롤러
package com.storeMap.controller;

import com.google.gson.Gson;
import com.storeMap.model.Store;
import com.storeMap.service.StoreService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/stores/*")
public class StoreController extends HttpServlet {
    
    private StoreService storeService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        storeService = new StoreService();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 전체 상점 목록 조회
                String zone = request.getParameter("zone");
                List<Store> stores;
                
                if (zone != null && !zone.isEmpty()) {
                    stores = storeService.getStoresByZone(Integer.parseInt(zone));
                } else {
                    stores = storeService.getAllStores();
                }
                
                out.print(gson.toJson(stores));
                
            } else if (pathInfo.startsWith("/search")) {
                // 검색
                String keyword = request.getParameter("keyword");
                List<Store> stores = storeService.searchStores(keyword);
                out.print(gson.toJson(stores));
                
            } else if (pathInfo.startsWith("/location")) {
                // 좌표로 상점 찾기
                int x = Integer.parseInt(request.getParameter("x"));
                int y = Integer.parseInt(request.getParameter("y"));
                int radius = Integer.parseInt(request.getParameter("radius"));
                
                Store store = storeService.getStoreByCoordinates(x, y, radius);
                if (store != null) {
                    out.print(gson.toJson(store));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Store not found\"}");
                }
                
            } else {
                // ID로 상점 조회
                String id = pathInfo.substring(1);
                Store store = storeService.getStoreById(Integer.parseInt(id));
                
                if (store != null) {
                    out.print(gson.toJson(store));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Store not found\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // JSON 요청 본문 읽기
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            Store store = gson.fromJson(sb.toString(), Store.class);
            int storeId = storeService.createStore(store);
            
            if (storeId > 0) {
                store.setStoreId(storeId);
                out.print(gson.toJson(store));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Failed to create store\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // JSON 요청 본문 읽기
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            Store store = gson.fromJson(sb.toString(), Store.class);
            boolean success = storeService.updateStore(store);
            
            if (success) {
                out.print("{\"success\": true}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Failed to update store\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                int storeId = Integer.parseInt(pathInfo.substring(1));
                boolean success = storeService.deleteStore(storeId);
                
                if (success) {
                    out.print("{\"success\": true}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Store not found\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid store ID\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}

// ========== Service 클래스 ==========
// StoreService.java
package com.storeMap.service;

import com.storeMap.dao.StoreDAO;
import com.storeMap.model.Store;
import java.sql.SQLException;
import java.util.List;

public class StoreService {
    
    private StoreDAO storeDAO;
    
    public StoreService() {
        this.storeDAO = new StoreDAO();
    }
    
    public List<Store> getAllStores() throws SQLException {
        return storeDAO.getAllStores();
    }
    
    public List<Store> getStoresByZone(int zoneNumber) throws SQLException {
        return storeDAO.getStoresByZone(zoneNumber);
    }
    
    public Store getStoreById(int storeId) throws SQLException {
        return storeDAO.getStoreById(storeId);
    }
    
    public Store getStoreByCoordinates(int x, int y, int radius) throws SQLException {
        return storeDAO.getStoreByCoordinates(x, y, radius);
    }
    
    public List<Store> searchStores(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStores();
        }
        return storeDAO.searchStores(keyword);
    }
    
    public int createStore(Store store) throws SQLException {
        // 유효성 검사
        validateStore(store);
        return storeDAO.insertStore(store);
    }
    
    public boolean updateStore(Store store) throws SQLException {
        // 유효성 검사
        validateStore(store);
        return storeDAO.updateStore(store);
    }
    
    public boolean deleteStore(int storeId) throws SQLException {
        return storeDAO.deleteStore(storeId);
    }
    
    private void validateStore(Store store) throws IllegalArgumentException {
        if (store.getStoreName() == null || store.getStoreName().trim().isEmpty()) {
            throw new IllegalArgumentException("상점명은 필수 입력 항목입니다.");
        }
        
        if (store.getXCoordinate() <= 0 || store.getYCoordinate() <= 0) {
            throw new IllegalArgumentException("유효한 위치 좌표를 설정해주세요.");
        }
        
        if (store.getZoneNumber() < 1 || store.getZoneNumber() > 5) {
            throw new IllegalArgumentException("유효한 구역 번호를 선택해주세요.");
        }
    }
}