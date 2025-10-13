package com.market_map.market_map.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.market_map.market_map.entity.Admin;
import com.market_map.market_map.repository.AdminRepository;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private AdminRepository adminRepository;
    
    /**
     * 비밀번호 인코더 Bean (평문 처리용)
     * @return NoOpPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }
    
    /**
     * 사용자 정보 서비스 Bean
     * @return UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Admin admin = adminRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
                
                return org.springframework.security.core.userdetails.User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword()) // NoOpPasswordEncoder가 평문 비밀번호를 처리합니다.
                    .roles("ADMIN")
                    .build();
            }
        };
    }
    
    /**
     * Security 필터 체인 설정
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 설정 오류
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (API 서버용)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 세션 관리 설정
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/admin/login?expired")
            )
            
            // 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 공개 접근 허용
                .requestMatchers(
                    "/",
                    "/static/**",
                    "/assets/**",
                    "/uploads/**",
                    "/api/stores/**",
                    "/admin/login",
                    "/admin/login.jsp"
                ).permitAll()
                
                // 관리자 페이지는 인증 필요
                .requestMatchers("/admin/**", "/api/admin/**")
                .authenticated()
                
                // 기타 모든 요청은 허용
                .anyRequest().permitAll()
            )
            
            // 로그인 설정
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/api/admin/login")
                .defaultSuccessUrl("/admin/store", true)
                .failureUrl("/admin/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            
            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/api/admin/logout")
                .logoutSuccessUrl("/admin/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // 예외 처리
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/admin/login?accessDenied=true")
            );
        
        return http.build();
    }
}
