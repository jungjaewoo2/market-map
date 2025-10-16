package com.market_map.market_map.service;

import com.market_map.market_map.entity.Admin;
import com.market_map.market_map.repository.AdminRepository;
import com.market_map.market_map.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 관련 비즈니스 로직 서비스
 */
@Service
@Transactional
public class AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 관리자 로그인 인증
     * @param username 사용자명
     * @param password 비밀번호
     * @return 인증된 관리자 정보 (인증 실패시 null)
     */
    public Admin authenticate(String username, String password) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            // 평문 비밀번호 비교 (개발용)
            if (password.equals(admin.getPassword())) {
                return admin;
            }
        }

        return null;
    }
    
    /**
     * 관리자 생성
     * @param admin 관리자 정보
     * @return 생성된 관리자 정보
     */
    public Admin createAdmin(Admin admin) {
        // 사용자명 중복 검사
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        // 이메일 중복 검사
        if (admin.getEmail() != null && adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        return adminRepository.save(admin);
    }
    
    /**
     * 관리자 정보 수정
     * @param admin 수정할 관리자 정보
     * @return 수정된 관리자 정보
     */
    public Admin updateAdmin(Admin admin) {
        Admin existingAdmin = adminRepository.findById(admin.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
        
        // 사용자명 변경 시 중복 검사
        if (!existingAdmin.getUsername().equals(admin.getUsername()) && 
            adminRepository.existsByUsername(admin.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }
        
        // 이메일 변경 시 중복 검사
        if (admin.getEmail() != null && 
            !admin.getEmail().equals(existingAdmin.getEmail()) && 
            adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        existingAdmin.setUsername(admin.getUsername());
        existingAdmin.setEmail(admin.getEmail());
        existingAdmin.setName(admin.getName());
        
        return adminRepository.save(existingAdmin);
    }
    
    /**
     * 비밀번호 변경
     * @param adminId 관리자 ID
     * @param oldPassword 기존 비밀번호
     * @param newPassword 새 비밀번호
     * @return 변경 성공 여부
     */
    public boolean changePassword(Long adminId, String oldPassword, String newPassword) {
        logger.info("=== AdminService.changePassword 호출 ===");
        logger.info("adminId: {}", adminId);
        logger.info("oldPassword 길이: {}", oldPassword != null ? oldPassword.length() : 0);
        logger.info("newPassword 길이: {}", newPassword != null ? newPassword.length() : 0);
        
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> {
                    logger.error("관리자를 찾을 수 없음: adminId={}", adminId);
                    return new IllegalArgumentException("존재하지 않는 관리자입니다.");
                });
        
        logger.info("관리자 조회 성공: username={}", admin.getUsername());
        logger.info("현재 저장된 비밀번호 길이: {}", admin.getPassword() != null ? admin.getPassword().length() : 0);

        // 기존 비밀번호 검증 (BCrypt 비교)
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            logger.warn("기존 비밀번호 불일치");
            throw new IllegalArgumentException("기존 비밀번호가 올바르지 않습니다.");
        }
        
        logger.info("기존 비밀번호 검증 성공");

        // 비밀번호 변경 (BCrypt 해시 저장)
        admin.setPassword(passwordEncoder.encode(newPassword));
        Admin savedAdmin = adminRepository.save(admin);
        
        logger.info("비밀번호 변경 완료: adminId={}", savedAdmin.getAdminId());
        logger.info("=== AdminService.changePassword 완료 ===");

        return true;
    }
    
    /**
     * 관리자 조회
     * @param adminId 관리자 ID
     * @return 관리자 정보
     */
    @Transactional(readOnly = true)
    public Admin getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
    }
    
    /**
     * 사용자명으로 관리자 조회
     * @param username 사용자명
     * @return 관리자 정보
     */
    @Transactional(readOnly = true)
    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }
    
    /**
     * 모든 관리자 목록 조회
     * @return 관리자 목록
     */
    @Transactional(readOnly = true)
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
    
    /**
     * 관리자 삭제
     * @param adminId 관리자 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteAdmin(Long adminId) {
        if (!adminRepository.existsById(adminId)) {
            throw new IllegalArgumentException("존재하지 않는 관리자입니다.");
        }
        
        // 마지막 관리자는 삭제 불가
        if (adminRepository.count() <= 1) {
            throw new IllegalStateException("마지막 관리자는 삭제할 수 없습니다.");
        }
        
        adminRepository.deleteById(adminId);
        return true;
    }
    
    /**
     * 관리자 존재 여부 확인
     * @param adminId 관리자 ID
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long adminId) {
        return adminRepository.existsById(adminId);
    }
    
    /**
     * 관리자 수 조회
     * @return 관리자 수
     */
    @Transactional(readOnly = true)
    public long countAdmins() {
        return adminRepository.count();
    }
}
