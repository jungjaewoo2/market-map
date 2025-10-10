package com.market_map.market_map.repository;

import com.market_map.market_map.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * 사용자명으로 관리자 조회
     * @param username 사용자명
     * @return Optional<Admin>
     */
    Optional<Admin> findByUsername(String username);
    
    /**
     * 이메일로 관리자 조회
     * @param email 이메일
     * @return Optional<Admin>
     */
    Optional<Admin> findByEmail(String email);
    
    /**
     * 사용자명이 존재하는지 확인
     * @param username 사용자명
     * @return boolean
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일이 존재하는지 확인
     * @param email 이메일
     * @return boolean
     */
    boolean existsByEmail(String email);
    
    /**
     * 사용자명과 비밀번호로 관리자 조회 (로그인용)
     * @param username 사용자명
     * @param password 비밀번호
     * @return Optional<Admin>
     */
    @Query("SELECT a FROM Admin a WHERE a.username = :username AND a.password = :password")
    Optional<Admin> findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    /**
     * 활성 관리자 수 조회
     * @return long
     */
    @Query("SELECT COUNT(a) FROM Admin a")
    long countActiveAdmins();
}
