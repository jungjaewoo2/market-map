package com.market_map.market_map.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs", indexes = {
    @Index(name = "idx_search_keyword", columnList = "search_keyword"),
    @Index(name = "idx_searched_at", columnList = "searched_at")
})
public class SearchLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    
    @Size(max = 100, message = "검색 키워드는 100자를 초과할 수 없습니다")
    @Column(name = "search_keyword", length = 100)
    private String searchKeyword;
    
    @Size(max = 20, message = "검색 타입은 20자를 초과할 수 없습니다")
    @Column(name = "search_type", length = 20)
    private String searchType;
    
    @Column(name = "result_count")
    private Integer resultCount;
    
    @CreationTimestamp
    @Column(name = "searched_at", nullable = false, updatable = false)
    private LocalDateTime searchedAt;
    
    // 생성자
    public SearchLog() {}
    
    public SearchLog(String searchKeyword, String searchType, Integer resultCount) {
        this.searchKeyword = searchKeyword;
        this.searchType = searchType;
        this.resultCount = resultCount;
    }
    
    // Getters and Setters
    public Long getLogId() {
        return logId;
    }
    
    public void setLogId(Long logId) {
        this.logId = logId;
    }
    
    public String getSearchKeyword() {
        return searchKeyword;
    }
    
    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }
    
    public String getSearchType() {
        return searchType;
    }
    
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
    
    public Integer getResultCount() {
        return resultCount;
    }
    
    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }
    
    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }
    
    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }
    
    @Override
    public String toString() {
        return "SearchLog{" +
                "logId=" + logId +
                ", searchKeyword='" + searchKeyword + '\'' +
                ", searchType='" + searchType + '\'' +
                ", resultCount=" + resultCount +
                ", searchedAt=" + searchedAt +
                '}';
    }
}
