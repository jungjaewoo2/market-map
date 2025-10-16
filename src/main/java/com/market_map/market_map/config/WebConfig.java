package com.market_map.market_map.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // assets 폴더 정적 리소스 핸들러 (WAR 배포용)
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("file:src/main/webapp/assets/")
                .setCachePeriod(3600);
        
        // uploads 폴더 정적 리소스 핸들러
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:src/main/webapp/uploads/")
                .setCachePeriod(0); // 캐시 비활성화
        
        // static 폴더 정적 리소스 핸들러 (기본)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
        
        // public 폴더 정적 리소스 핸들러 (기본)
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/")
                .setCachePeriod(3600);
    }
}
