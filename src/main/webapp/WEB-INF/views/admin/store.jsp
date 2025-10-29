<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>관리자 - 상점 위치 설정</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/assets/css/style.css" rel="stylesheet">
</head>
<body>
    <!-- 헤더 -->
    <div class="admin-header">
        <div class="container-fluid">
            <div class="d-flex justify-content-between align-items-center">
                <h2 class="mb-0">🏪 상점 관리 시스템</h2>
                <div>
                    <span class="me-3">관리자: <span id="adminName">admin</span></span>
                    <button class="btn btn-light btn-sm" onclick="location.href='/admin/change-password'">비밀번호 변경</button>
                    <button class="btn btn-light btn-sm ms-2" onclick="location.href='/admin/store'">상점 관리</button>
                    <button class="btn btn-danger btn-sm ms-2" onclick="logout()">로그아웃</button>
                </div>
            </div>
        </div>
    </div>

    <!-- 메인 컨테이너 -->
    <div class="main-container">
        <!-- 지도 영역 -->
        <div class="map-section">
            <h4 class="mb-3">📍 지도에서 위치 선택</h4>
            <div class="map-tools">
                <button class="tool-btn active" onclick="setMode('select')">
                    <i>📍</i> 위치 선택
                </button>
                <button class="tool-btn" onclick="setMode('move')">
                    <i>✋</i> 이동
                </button>
                <button class="tool-btn" onclick="zoomIn()">
                    <i>🔍</i> 확대
                </button>
                <button class="tool-btn" onclick="zoomOut()">
                    <i>🔍</i> 축소
                </button>
                <button class="tool-btn" onclick="resetView()">
                    <i>🔄</i> 초기화
                </button>
            </div>
            
            <div class="map-container-admin">
                <canvas id="adminMapCanvas"></canvas>
                <div class="coordinate-info">
                    <div>마우스 위치</div>
                    <div>X: <span id="mouseX">0</span>, Y: <span id="mouseY">0</span></div>
                </div>
                <div class="temp-marker" id="tempMarker" style="display: none;"></div>
            </div>
        </div>

        <!-- 폼 영역 -->
        <div class="form-section">
            <div class="form-header">
                <h4 class="mb-0">📝 상점 정보 입력</h4>
            </div>
            
            <form id="storeForm">
                <input type="hidden" id="storeId" name="storeId" value="">
                
                <!-- 좌표 표시 -->
                <div class="coordinate-display">
                    <div class="row">
                        <div class="col-6 text-center">
                            <div class="text-muted small">X 좌표</div>
                            <div class="coord-value" id="xCoordDisplay">-</div>
                        </div>
                        <div class="col-6 text-center">
                            <div class="text-muted small">Y 좌표</div>
                            <div class="coord-value" id="yCoordDisplay">-</div>
                        </div>
                    </div>
                    <input type="hidden" id="xCoordinate" name="xCoordinate">
                    <input type="hidden" id="yCoordinate" name="yCoordinate">
                </div>
                
                <div class="form-group">
                    <label class="form-label">상점명 *</label>
                    <input type="text" class="form-control" id="storeName" name="storeName" required>
                </div>
                
                <div class="row">
                    <div class="col-6">
                        <div class="form-group">
                            <label class="form-label">상점 코드</label>
                            <input type="text" class="form-control" id="storeCode" name="storeCode" 
                                   placeholder="예: 1-A01">
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="form-group">
                            <label class="form-label">구역</label>
                            <select class="form-control" id="zoneNumber" name="zoneNumber">
                                <option value="1">1지구</option>
                                <option value="2">2지구</option>
                                <option value="3">3지구</option>
                                <option value="4">4지구</option>
                                <option value="5">5지구</option>
                            </select>
                        </div>
                    </div>
                </div>
                
                
                <div class="form-group">
                    <label class="form-label">전화번호</label>
                    <input type="tel" class="form-control" id="phoneNumber" name="phoneNumber" 
                           placeholder="예: 02-1234-5678">
                </div>
                
                <div class="form-group">
                    <label class="form-label">영업시간</label>
                    <input type="text" class="form-control" id="businessHours" name="businessHours" 
                           placeholder="예: 09:00 - 18:00">
                </div>
                
                <div class="form-group">
                    <label class="form-label">설명</label>
                    <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                </div>
                
                <div class="form-group">
                    <label class="form-label">상점 이미지</label>
                    <div class="image-upload-area" onclick="document.getElementById('imageFiles').click()">
                        <i>📷</i>
                        <p class="mb-0">클릭하여 이미지 업로드</p>
                        <small class="text-muted">JPG, PNG (최대 5MB)</small>
                    </div>
                    <input type="file" id="imageFiles" name="images" multiple accept="image/*" 
                           style="display: none;" onchange="previewImages(this)">
                    <div class="image-preview" id="imagePreview"></div>
                </div>
                
                <button type="submit" class="btn-submit">저장하기</button>
                <button type="button" class="btn-cancel" onclick="resetForm()">취소</button>
            </form>
        </div>
    </div>

    <!-- 상점 목록 -->
    <div class="container-fluid">
        <div class="store-list-section">
            <h4 class="mb-3">📋 등록된 상점 목록</h4>
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>코드</th>
                            <th>상점명</th>
                            <th>구역</th>
                            <th>전화번호</th>
                            <th>좌표</th>
                            <th>상태</th>
                            <th>관리</th>
                        </tr>
                    </thead>
                    <tbody id="storeTableBody">
                        <!-- 동적으로 로드됨 -->
                    </tbody>
                </table>
            </div>
            <!-- 페이징 영역 -->
            <div id="paginationContainer" class="mt-3">
                <!-- 동적으로 생성됨 -->
            </div>
        </div>
    </div>

    <!-- 알림 메시지 -->
    <div class="alert-custom" id="alertMessage"></div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/assets/js/admin.js?v=4"></script>
</body>
</html>