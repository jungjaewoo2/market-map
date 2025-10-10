package com.market_map.market_map.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import com.google.gson.annotations.Expose;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_images", indexes = {
    @Index(name = "idx_store_images", columnList = "store_id")
})
public class StoreImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    
    @NotBlank(message = "이미지 URL은 필수입니다")
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType = ImageType.SUB;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    // 이미지 타입 열거형
    public enum ImageType {
        MAIN, SUB
    }
    
    // 생성자
    public StoreImage() {}
    
    public StoreImage(Store store, String imageUrl, ImageType imageType) {
        this.store = store;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.isActive = true;
    }
    
    public StoreImage(Store store, String imageUrl, ImageType imageType, Integer displayOrder) {
        this.store = store;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.displayOrder = displayOrder;
        this.isActive = true;
    }
    
    public StoreImage(Store store, String imageUrl, ImageType imageType, Integer displayOrder, Boolean isActive) {
        this.store = store;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public Long getImageId() {
        return imageId;
    }
    
    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
    
    public Store getStore() {
        return store;
    }
    
    public void setStore(Store store) {
        this.store = store;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public ImageType getImageType() {
        return imageType;
    }
    
    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    @Override
    public String toString() {
        return "StoreImage{" +
                "imageId=" + imageId +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageType=" + imageType +
                ", displayOrder=" + displayOrder +
                ", isActive=" + isActive +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
