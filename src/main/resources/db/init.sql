-- 관문상가시장 지도 서비스 데이터베이스 스키마
-- 데이터베이스: market_map

-- 관리자 테이블
CREATE TABLE IF NOT EXISTS admin_users (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 상가 지도 이미지 관리 테이블
CREATE TABLE IF NOT EXISTS market_maps (
    map_id INT PRIMARY KEY AUTO_INCREMENT,
    map_name VARCHAR(100) NOT NULL,
    map_image_url VARCHAR(500) NOT NULL,
    map_width INT NOT NULL,
    map_height INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 상점 정보 테이블 (이미지 좌표 기반)
CREATE TABLE IF NOT EXISTS stores (
    store_id INT PRIMARY KEY AUTO_INCREMENT,
    store_name VARCHAR(100) NOT NULL,
    store_code VARCHAR(50),
    zone_number INT,
    phone_number VARCHAR(20),
    address VARCHAR(255),
    detail_address VARCHAR(100),
    map_id INT DEFAULT 1,
    x_coordinate INT,
    y_coordinate INT,
    marker_radius INT DEFAULT 10,
    business_hours VARCHAR(200),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES admin_users(admin_id),
    FOREIGN KEY (map_id) REFERENCES market_maps(map_id),
    INDEX idx_coordinates (x_coordinate, y_coordinate),
    INDEX idx_store_name (store_name),
    INDEX idx_zone (zone_number),
    FULLTEXT KEY ft_store_name (store_name)
);

-- 상점 이미지 테이블
CREATE TABLE IF NOT EXISTS store_images (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    store_id INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_type ENUM('main', 'sub') DEFAULT 'sub',
    display_order INT DEFAULT 0,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES stores(store_id) ON DELETE CASCADE,
    INDEX idx_store_images (store_id)
);

-- 검색 로그 테이블
CREATE TABLE IF NOT EXISTS search_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    search_keyword VARCHAR(100),
    search_type VARCHAR(20),
    result_count INT,
    searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_search_keyword (search_keyword),
    INDEX idx_searched_at (searched_at)
);

-- 초기 데이터 삽입

-- 기본 지도 이미지 설정
INSERT INTO market_maps (map_name, map_image_url, map_width, map_height) VALUES
('관문상가시장 안내도', '/static/images/map.png', 1920, 1080);

-- 초기 관리자 계정 생성 (비밀번호: admin123)
INSERT INTO admin_users (username, password, email, name) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin@market-map.com', '시스템관리자');

-- 샘플 상점 데이터
INSERT INTO stores (store_name, store_code, zone_number, phone_number, address, detail_address, 
                   map_id, x_coordinate, y_coordinate, business_hours, description, created_by) VALUES
('맛있는 김밥천국', '1-A01', 1, '031-123-4567', '경기도 구리시 인창동 123-45', '1지구 A구역 1번', 
 1, 450, 320, '평일 07:00-22:00, 주말 08:00-21:00', '다양한 분식과 한식을 제공하는 음식점입니다.', 1),
('스타벅스 구리역점', '2-B05', 2, '031-234-5678', '경기도 구리시 수택동 234-56', '2지구 B구역 5번', 
 1, 650, 450, '매일 07:00-23:00', '편안한 분위기의 커피 전문점입니다.', 1),
('GS25 편의점', '3-C10', 3, '031-345-6789', '경기도 구리시 인창동 345-67', '3지구 C구역 10번', 
 1, 320, 550, '24시간', '편의점 및 간편식품 판매', 1),
('정형외과 의원', '1-D03', 1, '031-456-7890', '경기도 구리시 인창동 456-78', '1지구 D구역 3번', 
 1, 580, 380, '평일 09:00-18:00, 토요일 09:00-13:00', '정형외과 전문의원', 1),
('미용실 뷰티샵', '4-E08', 4, '031-567-8901', '경기도 구리시 수택동 567-89', '4지구 E구역 8번', 
 1, 420, 620, '평일 10:00-20:00, 일요일 휴무', '헤어컷 및 펌 서비스', 1);
