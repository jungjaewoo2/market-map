// ========== 인증 필터 ==========
// AuthenticationFilter.java
package com.storeMap.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/admin/*")
public class AuthenticationFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 필터 초기화
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // 로그인 페이지는 제외
        if (path.endsWith("/login") || path.endsWith("/login.jsp")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 세션 확인
        HttpSession session = httpRequest.getSession(false);
        
        if (session != null && session.getAttribute("adminUser") != null) {
            // 로그인된 상태 - 요청 처리 계속
            chain.doFilter(request, response);
        } else {
            // 로그인되지 않은 상태 - 로그인 페이지로 리다이렉트
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }
    
    @Override
    public void destroy() {
        // 필터 종료
    }
}

// ========== 인코딩 필터 ==========
// EncodingFilter.java
package com.storeMap.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*")
public class EncodingFilter implements Filter {
    
    private String encoding = "UTF-8";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String paramEncoding = filterConfig.getInitParameter("encoding");
        if (paramEncoding != null) {
            encoding = paramEncoding;
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        // 필터 종료
    }
}

// ========== CORS 필터 (API용) ==========
// CorsFilter.java
package com.storeMap.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/api/*")
public class CorsFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        chain.doFilter(request, response);
    }
}

// ========== 로그인 컨트롤러 ==========
// LoginController.java
package com.storeMap.controller;

import com.storeMap.model.Admin;
import com.storeMap.service.AdminService;
import com.storeMap.util.PasswordUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginController extends HttpServlet {
    
    private AdminService adminService;
    
    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 로그인 페이지로 포워드
        request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        try {
            Admin admin = adminService.authenticate(username, password);
            
            if (admin != null) {
                // 로그인 성공 - 세션 생성
                HttpSession session = request.getSession();
                session.setAttribute("adminUser", admin);
                session.setAttribute("adminId", admin.getAdminId());
                session.setAttribute("adminName", admin.getName());
                
                // 세션 타임아웃 설정 (30분)
                session.setMaxInactiveInterval(30 * 60);
                
                // JSON 응답
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": true}");
                
            } else {
                // 로그인 실패
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": false, \"message\": \"아이디 또는 비밀번호가 올바르지 않습니다.\"}");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"서버 오류가 발생했습니다.\"}");
        }
    }
}

// ========== 로그아웃 컨트롤러 ==========
// LogoutController.java
package com.storeMap.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        response.sendRedirect(request.getContextPath() + "/login");
    }
}

// ========== 파일 업로드 서비스 ==========
// FileUploadService.java
package com.storeMap.service;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUploadService {
    
    private static final String UPLOAD_DIR = "uploads/stores";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_REQUEST_SIZE = 20 * 1024 * 1024; // 20MB
    
    public List<String> uploadImages(HttpServletRequest request, int storeId) throws Exception {
        List<String> uploadedFiles = new ArrayList<>();
        
        // 업로드 디렉토리 확인
        String uploadPath = request.getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 파일 업로드 설정
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024 * 1024); // 1MB
        
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);
        upload.setHeaderEncoding("UTF-8");
        
        // 파일 처리
        List<FileItem> formItems = upload.parseRequest(request);
        
        for (FileItem item : formItems) {
            if (!item.isFormField() && item.getSize() > 0) {
                String fileName = item.getName();
                String fileExtension = getFileExtension(fileName);
                
                // 허용된 확장자 확인
                if (!isAllowedExtension(fileExtension)) {
                    continue;
                }
                
                // 고유한 파일명 생성
                String newFileName = "store_" + storeId + "_" + UUID.randomUUID().toString() + fileExtension;
                String filePath = uploadPath + File.separator + newFileName;
                
                // 파일 저장
                File storeFile = new File(filePath);
                item.write(storeFile);
                
                // 웹 경로 저장
                uploadedFiles.add(UPLOAD_DIR + "/" + newFileName);
            }
        }
        
        return uploadedFiles;
    }
    
    private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
    
    private boolean isAllowedExtension(String extension) {
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean deleteImage(HttpServletRequest request, String imagePath) {
        try {
            String fullPath = request.getServletContext().getRealPath("") + File.separator + imagePath;
            File file = new File(fullPath);
            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

// ========== 비밀번호 유틸리티 ==========
// PasswordUtil.java
package com.storeMap.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    
    // 비밀번호 해시화
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
    
    // 비밀번호 검증
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
    
    // SHA-256 해시 (레거시 호환용)
    public static String sha256(String text) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

// ========== 관리자 서비스 ==========
// AdminService.java
package com.storeMap.service;

import com.storeMap.dao.AdminDAO;
import com.storeMap.model.Admin;
import com.storeMap.util.PasswordUtil;
import java.sql.SQLException;

public class AdminService {
    
    private AdminDAO adminDAO;
    
    public AdminService() {
        this.adminDAO = new AdminDAO();
    }
    
    public Admin authenticate(String username, String password) throws SQLException {
        Admin admin = adminDAO.getAdminByUsername(username);
        
        if (admin != null) {
            // 비밀번호 검증 (BCrypt)
            if (admin.getPassword().startsWith("$2")) {
                // BCrypt 해시
                if (PasswordUtil.verifyPassword(password, admin.getPassword())) {
                    return admin;
                }
            } else {
                // SHA-256 해시 (레거시)
                if (admin.getPassword().equals(PasswordUtil.sha256(password))) {
                    // 보안 향상을 위해 BCrypt로 업데이트
                    admin.setPassword(PasswordUtil.hashPassword(password));
                    adminDAO.updatePassword(admin.getAdminId(), admin.getPassword());
                    return admin;
                }
            }
        }
        
        return null;
    }
    
    public boolean createAdmin(Admin admin) throws SQLException {
        // 비밀번호 해시화
        admin.setPassword(PasswordUtil.hashPassword(admin.getPassword()));
        return adminDAO.insertAdmin(admin) > 0;
    }
    
    public boolean updateAdmin(Admin admin) throws SQLException {
        return adminDAO.updateAdmin(admin);
    }
    
    public boolean changePassword(int adminId, String oldPassword, String newPassword) throws SQLException {
        Admin admin = adminDAO.getAdminById(adminId);
        
        if (admin != null && PasswordUtil.verifyPassword(oldPassword, admin.getPassword())) {
            String hashedNewPassword = PasswordUtil.hashPassword(newPassword);
            return adminDAO.updatePassword(adminId, hashedNewPassword);
        }
        
        return false;
    }
}