package com.market_map.market_map.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "market_maps")
public class MarketMap {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Long mapId;
    
    @NotBlank(message = "지도명은 필수입니다")
    @Column(name = "map_name", nullable = false, length = 100)
    private String mapName;
    
    @NotBlank(message = "지도 이미지 URL은 필수입니다")
    @Column(name = "map_image_url", nullable = false, length = 500)
    private String mapImageUrl;
    
    @NotNull(message = "지도 너비는 필수입니다")
    @Column(name = "map_width", nullable = false)
    private Integer mapWidth;
    
    @NotNull(message = "지도 높이는 필수입니다")
    @Column(name = "map_height", nullable = false)
    private Integer mapHeight;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 생성자
    public MarketMap() {}
    
    public MarketMap(String mapName, String mapImageUrl, Integer mapWidth, Integer mapHeight) {
        this.mapName = mapName;
        this.mapImageUrl = mapImageUrl;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }
    
    // Getters and Setters
    public Long getMapId() {
        return mapId;
    }
    
    public void setMapId(Long mapId) {
        this.mapId = mapId;
    }
    
    public String getMapName() {
        return mapName;
    }
    
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
    
    public String getMapImageUrl() {
        return mapImageUrl;
    }
    
    public void setMapImageUrl(String mapImageUrl) {
        this.mapImageUrl = mapImageUrl;
    }
    
    public Integer getMapWidth() {
        return mapWidth;
    }
    
    public void setMapWidth(Integer mapWidth) {
        this.mapWidth = mapWidth;
    }
    
    public Integer getMapHeight() {
        return mapHeight;
    }
    
    public void setMapHeight(Integer mapHeight) {
        this.mapHeight = mapHeight;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "MarketMap{" +
                "mapId=" + mapId +
                ", mapName='" + mapName + '\'' +
                ", mapImageUrl='" + mapImageUrl + '\'' +
                ", mapWidth=" + mapWidth +
                ", mapHeight=" + mapHeight +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
