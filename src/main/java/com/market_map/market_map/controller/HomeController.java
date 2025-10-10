package com.market_map.market_map.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 메인 홈 컨트롤러
 */
@Controller
public class HomeController {
    
    /**
     * 메인 페이지 (사용자용 지도 페이지)
     * @return index.jsp
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
}
