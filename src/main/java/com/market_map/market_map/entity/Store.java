package com.market_map.market_map.entity;

import jakarta.persistence.*;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.google.gson.annotations.Expose;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "stores", indexes = {
    @Index(name = "idx_coordinates", columnList = "x_coordinate, y_coordinate"),
    @Index(name = "idx_store_name", columnList = "store_name"),
    @Index(name = "idx_zone", columnList = "zone_number")
})
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;
    
    @NotBlank(message = "상점명은 필수입니다")
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;
    
    @Column(name = "store_code", length = 50)
    private String storeCode;
    
    @Column(name = "zone_number")
    private Integer zoneNumber;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "address", length = 255)
    private String address;
    
    @Column(name = "detail_address", length = 100)
    private String detailAddress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_id")
    @Expose(serialize = false, deserialize = false)
    private MarketMap marketMap;
    
    @NotNull(message = "X 좌표는 필수입니다")
    @Column(name = "x_coordinate", nullable = false)
    private Integer xCoordinate;
    
    @NotNull(message = "Y 좌표는 필수입니다")
    @Column(name = "y_coordinate", nullable = false)
    private Integer yCoordinate;
    
    @Column(name = "marker_radius")
    private Integer markerRadius = 10;
    
    @Column(name = "business_hours", length = 200)
    private String businessHours;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @Expose(serialize = false, deserialize = false)
    private Admin createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Transient
    @Expose(serialize = false, deserialize = false)
    private List<MultipartFile> images = new ArrayList<>();
    
    
    // 생성자
    public Store() {}
    
    public Store(String storeName, String storeCode, Integer zoneNumber, 
                Integer xCoordinate, Integer yCoordinate) {
        this.storeName = storeName;
        this.storeCode = storeCode;
        this.zoneNumber = zoneNumber;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }
    
    // Getters and Setters
    public Long getStoreId() {
        return storeId;
    }
    
    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    public String getStoreCode() {
        return storeCode;
    }
    
    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }
    
    public Integer getZoneNumber() {
        return zoneNumber;
    }
    
    public void setZoneNumber(Integer zoneNumber) {
        this.zoneNumber = zoneNumber;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDetailAddress() {
        return detailAddress;
    }
    
    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }
    
    public MarketMap getMarketMap() {
        return marketMap;
    }
    
    public void setMarketMap(MarketMap marketMap) {
        this.marketMap = marketMap;
    }
    
    public Integer getXCoordinate() {
        return xCoordinate;
    }
    
    public void setXCoordinate(Integer xCoordinate) {
        this.xCoordinate = xCoordinate;
    }
    
    public Integer getYCoordinate() {
        return yCoordinate;
    }
    
    public void setYCoordinate(Integer yCoordinate) {
        this.yCoordinate = yCoordinate;
    }
    
    public Integer getMarkerRadius() {
        return markerRadius;
    }
    
    public void setMarkerRadius(Integer markerRadius) {
        this.markerRadius = markerRadius;
    }
    
    public String getBusinessHours() {
        return businessHours;
    }
    
    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Admin getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Admin createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<MultipartFile> getImages() {
        return images;
    }
    
    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }
    
    
    @Override
    public String toString() {
        return "Store{" +
                "storeId=" + storeId +
                ", storeName='" + storeName + '\'' +
                ", storeCode='" + storeCode + '\'' +
                ", zoneNumber=" + zoneNumber +
                ", xCoordinate=" + xCoordinate +
                ", yCoordinate=" + yCoordinate +
                ", isActive=" + isActive +
                '}';
    }
}
