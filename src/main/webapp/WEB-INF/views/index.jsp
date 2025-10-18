<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>관문상가시장 안내도</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/assets/css/style.css" rel="stylesheet">
</head>
<body>
    <!-- 헤더 -->
    <br>
    <br>
    <br>
    
    <div class="header">
        <div class="container">
            <h1 class="text-center mb-3" style="color: #667eea; font-weight: 700;">🏪 관문상가시장 안내</h1>
            <div class="search-container">
                <div class="search-box">
                    <input type="text" id="searchInput" placeholder="상점명을 검색하세요..." autocomplete="off">
                    <button class="search-btn" onclick="searchStore()">검색</button>
                    <button class="search-reset-btn" onclick="resetSearch()">초기화</button>
                </div>
            </div>
        </div>
    </div>

    <!-- 지도 컨테이너 -->
    <div class="map-container">
        <!-- 지도 영역 -->
        <div class="map-wrapper">
            <div class="map-canvas-container">
                <canvas id="mapCanvas"></canvas>
            </div>
        </div>

        <!-- 사이드바 -->
        <div class="sidebar">
            <h5 style="font-weight: 700; margin-bottom: 15px;">📍 구역별 상점</h5>
            <div class="zone-tabs">
                <div class="zone-tab active" onclick="filterByZone(0)">전체</div>
                <div class="zone-tab" onclick="filterByZone(1)">1지구</div>
                <div class="zone-tab" onclick="filterByZone(2)">2지구</div>
                <div class="zone-tab" onclick="filterByZone(3)">3지구</div>
                <div class="zone-tab" onclick="filterByZone(4)">4지구</div>
                <div class="zone-tab" onclick="filterByZone(5)">5지구</div>
            </div>
            <div class="store-list" id="storeList">
                <!-- 상점 목록이 동적으로 로드됨 -->
            </div>
        </div>
    </div>

    <!-- 상점 상세 팝업 -->
    <div class="popup-overlay" onclick="closePopup()"></div>
    <div class="store-popup" id="storePopup">
        <div class="popup-header">
            <h3 class="popup-title" id="popupTitle">상점명</h3>
            <button class="popup-close" onclick="closePopup()">×</button>
        </div>
        <div class="popup-content">
            <div class="popup-info-item">
                <div class="popup-info-icon">📞</div>
                <div>
                    <div style="color: #999; font-size: 14px;">전화번호</div>
                    <div id="popupPhone">031-123-4567</div>
                </div>
            </div>
            <div class="popup-info-item">
                <div class="popup-info-icon">🏢</div>
                <div>
                    <div style="color: #999; font-size: 14px;">구역</div>
                    <div id="popupZone">1지구</div>
                </div>
            </div>
            <div class="popup-info-item">
                <div class="popup-info-icon">⏰</div>
                <div>
                    <div style="color: #999; font-size: 14px;">영업시간</div>
                    <div id="popupHours">09:00 - 21:00</div>
                </div>
            </div>
            <div class="popup-info-item">
                <div class="popup-info-icon">📝</div>
                <div>
                    <div style="color: #999; font-size: 14px;">설명</div>
                    <div id="popupDescription">상점 설명</div>
                </div>
            </div>
            <div class="popup-images" id="popupImages">
                <!-- 이미지가 동적으로 로드됨 -->
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/assets/js/map.js?v=13"></script>
</body>
</html>