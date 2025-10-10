package com.market_map.market_map.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 관리자 페이지 뷰 컨트롤러
 */
@Controller
public class AdminViewController {
    
    /**
     * 관리자 로그인 페이지
     */
    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/login";
    }
    
    
    /**
     * 관리자 대시보드 페이지 (상점 관리로 리다이렉트)
     */
    @GetMapping("/admin/dashboard")
    public String dashboardPage() {
        return "redirect:/admin/store";
    }
    
    /**
     * 관리자 상점 관리 페이지
     */
    @GetMapping("/admin/store")
    public String storePage() {
        return "admin/store";
    }
    
    /**
     * 관리자 비밀번호 변경 페이지
     */
    @GetMapping("/admin/change-password")
    public String changePasswordPage() {
        return "admin/change-password";
    }
}
