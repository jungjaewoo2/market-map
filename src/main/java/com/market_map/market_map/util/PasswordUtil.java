package com.market_map.market_map.util;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 암호화 및 검증 유틸리티
 */
@Component
public class PasswordUtil {
    
    /**
     * 비밀번호를 BCrypt로 해시화
     * @param plainPassword 평문 비밀번호
     * @return 해시화된 비밀번호
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
    
    /**
     * 비밀번호 검증
     * @param plainPassword 평문 비밀번호
     * @param hashedPassword 해시화된 비밀번호
     * @return 검증 결과
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * SHA-256 해시 (레거시 호환용)
     * @param text 해시할 텍스트
     * @return SHA-256 해시값
     */
    public static String sha256(String text) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 해시 생성 중 오류 발생", e);
        }
    }
    
    /**
     * 비밀번호 강도 검증
     * @param password 비밀번호
     * @return 검증 결과 메시지
     */
    public static String validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return "비밀번호는 최소 8자 이상이어야 합니다.";
        }
        
        if (password.length() > 50) {
            return "비밀번호는 최대 50자까지 가능합니다.";
        }
        
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        if (!hasUpperCase) {
            return "비밀번호는 대문자를 포함해야 합니다.";
        }
        
        if (!hasLowerCase) {
            return "비밀번호는 소문자를 포함해야 합니다.";
        }
        
        if (!hasDigit) {
            return "비밀번호는 숫자를 포함해야 합니다.";
        }
        
        if (!hasSpecialChar) {
            return "비밀번호는 특수문자를 포함해야 합니다.";
        }
        
        return "valid";
    }
}
