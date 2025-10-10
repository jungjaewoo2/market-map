-- 초기 데이터 삽입 (애플리케이션 시작 시 자동 실행)

-- 기본 지도 이미지 설정
INSERT IGNORE INTO market_maps (map_id, map_name, map_image_url, map_width, map_height) VALUES
(1, '관문상가시장 안내도', '/static/images/map.png', 1920, 1080);

-- 초기 관리자 계정 생성 (비밀번호: admin123!)
INSERT IGNORE INTO admin_users (admin_id, username, password, email, name) VALUES 
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin@market-map.com', '시스템관리자');

-- 샘플 상점 데이터
INSERT IGNORE INTO stores (store_id, store_name, store_code, zone_number, phone_number, address, detail_address, 
                   map_id, x_coordinate, y_coordinate, business_hours, description, created_by) VALUES
(1, '맛있는 김밥천국', '1-A01', 1, '031-123-4567', '경기도 구리시 인창동 123-45', '1지구 A구역 1번', 
 1, 450, 320, '평일 07:00-22:00, 주말 08:00-21:00', '다양한 분식과 한식을 제공하는 음식점입니다.', 1),
(2, '스타벅스 구리역점', '2-B05', 2, '031-234-5678', '경기도 구리시 수택동 234-56', '2지구 B구역 5번', 
 1, 650, 450, '매일 07:00-23:00', '편안한 분위기의 커피 전문점입니다.', 1),
(3, 'GS25 편의점', '3-C10', 3, '031-345-6789', '경기도 구리시 인창동 345-67', '3지구 C구역 10번', 
 1, 320, 550, '24시간', '편의점 및 간편식품 판매', 1),
(4, '정형외과 의원', '1-D03', 1, '031-456-7890', '경기도 구리시 인창동 456-78', '1지구 D구역 3번', 
 1, 580, 380, '평일 09:00-18:00, 토요일 09:00-13:00', '정형외과 전문의원', 1),
(5, '미용실 뷰티샵', '4-E08', 4, '031-567-8901', '경기도 구리시 수택동 567-89', '4지구 E구역 8번', 
 1, 420, 620, '평일 10:00-20:00, 일요일 휴무', '헤어컷 및 펌 서비스', 1);
