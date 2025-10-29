/**
 * 관리자 페이지 JavaScript
 */

// 전역 변수
let canvas, ctx;
let mapImage = new Image();
let mode = 'select'; // select, move
let scale = 1;
let offsetX = 0, offsetY = 0;
let isDragging = false;
let dragStartX, dragStartY;
let selectedX = null, selectedY = null;
let stores = [];

/**
 * 페이지 로드 시 초기화
 */
window.addEventListener('DOMContentLoaded', function() {
    initializeAdminMap();
    loadStores();
    setupEventListeners();
});

/**
 * 지도 초기화
 */
function initializeAdminMap() {
    canvas = document.getElementById('adminMapCanvas');
    if (!canvas) return;
    
    ctx = canvas.getContext('2d');
    
    // 지도 이미지 로드 - 여러 경로 시도
    const imagePaths = [
        '/assets/images/map.png',
        './assets/images/map.png',
        'assets/images/map.png'
    ];
    
    let currentPathIndex = 0;
    
    function tryLoadImage() {
        if (currentPathIndex < imagePaths.length) {
            mapImage.src = imagePaths[currentPathIndex];
            console.log('관리자 페이지 이미지 로딩 시도:', imagePaths[currentPathIndex]);
        }
    }
    
    tryLoadImage();
    mapImage.onload = function() {
        console.log('관리자 페이지 지도 이미지 로드 성공:', mapImage.src);
        console.log('이미지 크기:', mapImage.width, 'x', mapImage.height);
        
        canvas.width = mapImage.width;
        canvas.height = mapImage.height;
        fitCanvasToContainer();
        drawAdminMap();
    };
    
    mapImage.onerror = function() {
        console.error('관리자 페이지 지도 이미지 로드 실패:', mapImage.src);
        
        // 다음 경로 시도
        currentPathIndex++;
        if (currentPathIndex < imagePaths.length) {
            console.log('관리자 페이지 다음 경로로 재시도:', imagePaths[currentPathIndex]);
            tryLoadImage();
        } else {
            console.error('관리자 페이지 모든 경로에서 이미지 로드 실패');
            // 기본 배경색 설정
            if (ctx) {
                ctx.fillStyle = '#f0f0f0';
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                ctx.fillStyle = '#666';
                ctx.font = '16px Arial';
                ctx.textAlign = 'center';
                ctx.fillText('지도 이미지를 불러올 수 없습니다', canvas.width/2, canvas.height/2);
            }
        }
    };
    
    // 이벤트 리스너
    canvas.addEventListener('click', handleMapClick);
    canvas.addEventListener('mousemove', handleMouseMove);
    canvas.addEventListener('mousedown', startDrag);
    canvas.addEventListener('mousemove', drag);
    canvas.addEventListener('mouseup', endDrag);
}

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 폼 제출 이벤트
    const form = document.getElementById('storeForm');
    if (form) {
        form.addEventListener('submit', saveStore);
    }
}

/**
 * 캔버스 크기 조정
 */
function fitCanvasToContainer() {
    const container = document.querySelector('.map-container-admin');
    if (!container) return;
    
    // 컨테이너의 실제 가용 너비 계산 (패딩, 보더 제외)
    const containerWidth = container.clientWidth - 4; // 패딩 제외
    const containerHeight = window.innerHeight * 0.8; // 화면 높이의 80%
    
    const aspectRatio = mapImage.width / mapImage.height;
    
    // 컨테이너 가로 사이즈에 맞게 조정 (가로폭 100% 우선)
    let newWidth = containerWidth;
    let newHeight = newWidth / aspectRatio;
    
    // 높이가 최대 높이를 초과해도 가로폭을 우선시
    // 스크롤로 처리하도록 함
    
    // 캔버스 표시 크기 설정
    canvas.style.width = newWidth + 'px';
    canvas.style.height = newHeight + 'px';
    
    // 캔버스 실제 크기 설정 (고해상도 디스플레이 대응)
    const devicePixelRatio = window.devicePixelRatio || 1;
    canvas.width = newWidth * devicePixelRatio;
    canvas.height = newHeight * devicePixelRatio;
    
    // 컨텍스트 스케일 조정
    ctx.scale(devicePixelRatio, devicePixelRatio);
    
    // 스케일 비율 계산 (이미지 크기 기준)
    scale = newWidth / mapImage.width;
    
    // 오프셋 초기화하여 이미지가 캔버스 중앙에 위치하도록 함
    offsetX = 0;
    offsetY = 0;
    
    console.log(`캔버스 크기 조정: ${newWidth}x${newHeight}, 스케일: ${scale.toFixed(3)}`);
    
    drawAdminMap();
}

/**
 * 지도 그리기
 */
function drawAdminMap() {
    if (!ctx || !mapImage) return;
    
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.save();
    ctx.translate(offsetX, offsetY);
    ctx.scale(scale, scale);
    ctx.drawImage(mapImage, 0, 0);
    
    // 기존 상점 마커 그리기
    drawExistingMarkers();
    
    // 선택된 위치 마커 그리기
    if (selectedX !== null && selectedY !== null) {
        ctx.fillStyle = '#ff0000';
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.arc(selectedX, selectedY, 15, 0, 2 * Math.PI);
        ctx.fill();
        ctx.stroke();
        
        // 십자선
        ctx.strokeStyle = '#ff0000';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(selectedX - 20, selectedY);
        ctx.lineTo(selectedX + 20, selectedY);
        ctx.moveTo(selectedX, selectedY - 20);
        ctx.lineTo(selectedX, selectedY + 20);
        ctx.stroke();
    }
    
    ctx.restore();
}

/**
 * 기존 상점 마커 그리기
 */
function drawExistingMarkers() {
    if (!ctx) return;
    
    stores.forEach(store => {
        ctx.save();
        
        // 노란색 원형 마커
        ctx.fillStyle = '#FFD700'; // 골드 노란색
        ctx.globalAlpha = 0.9;
        ctx.beginPath();
        ctx.arc(store.xCoordinate, store.yCoordinate, 12, 0, 2 * Math.PI);
        ctx.fill();
        
        // 검은색 테두리 추가 (더 잘 보이도록)
        ctx.strokeStyle = '#000000';
        ctx.lineWidth = 2;
        ctx.globalAlpha = 1;
        ctx.beginPath();
        ctx.arc(store.xCoordinate, store.yCoordinate, 12, 0, 2 * Math.PI);
        ctx.stroke();
        
        ctx.restore();
    });
}

/**
 * 모드 설정
 */
function setMode(newMode) {
    mode = newMode;
    document.querySelectorAll('.tool-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.closest('.tool-btn').classList.add('active');
    
    canvas.style.cursor = mode === 'select' ? 'crosshair' : 'grab';
}

/**
 * 지도 클릭 처리
 */
function handleMapClick(e) {
    if (mode !== 'select') return;
    
    const rect = canvas.getBoundingClientRect();
    const x = (e.clientX - rect.left) * (canvas.width / rect.width) / scale - offsetX / scale;
    const y = (e.clientY - rect.top) * (canvas.height / rect.height) / scale - offsetY / scale;
    
    // 좌표 설정
    selectedX = Math.round(x);
    selectedY = Math.round(y);
    
    // 좌표 표시 업데이트
    document.getElementById('xCoordDisplay').textContent = selectedX;
    document.getElementById('yCoordDisplay').textContent = selectedY;
    document.getElementById('xCoordinate').value = selectedX;
    document.getElementById('yCoordinate').value = selectedY;
    
    // 임시 마커 표시
    const tempMarker = document.getElementById('tempMarker');
    if (tempMarker) {
        tempMarker.style.display = 'block';
        tempMarker.style.left = (e.clientX - rect.left) + 'px';
        tempMarker.style.top = (e.clientY - rect.top) + 'px';
    }
    
    drawAdminMap();
    showAlert('위치가 선택되었습니다.', 'success');
}

/**
 * 마우스 이동 처리
 */
function handleMouseMove(e) {
    const rect = canvas.getBoundingClientRect();
    const x = Math.round((e.clientX - rect.left) * (canvas.width / rect.width) / scale - offsetX / scale);
    const y = Math.round((e.clientY - rect.top) * (canvas.height / rect.height) / scale - offsetY / scale);
    
    const mouseXElement = document.getElementById('mouseX');
    const mouseYElement = document.getElementById('mouseY');
    if (mouseXElement) mouseXElement.textContent = x;
    if (mouseYElement) mouseYElement.textContent = y;
}

/**
 * 드래그 기능
 */
function startDrag(e) {
    if (mode !== 'move') return;
    isDragging = true;
    dragStartX = e.clientX - offsetX;
    dragStartY = e.clientY - offsetY;
    canvas.style.cursor = 'grabbing';
}

function drag(e) {
    if (!isDragging || mode !== 'move') return;
    
    e.preventDefault();
    offsetX = e.clientX - dragStartX;
    offsetY = e.clientY - dragStartY;
    drawAdminMap();
}

function endDrag() {
    isDragging = false;
    if (mode === 'move') {
        canvas.style.cursor = 'grab';
    }
}

/**
 * 줌 기능
 */
function zoomIn() {
    scale = Math.min(scale * 1.2, 5); // 최대 확대 비율을 5로 증가
    drawAdminMap();
}

function zoomOut() {
    scale = Math.max(scale * 0.8, 0.3); // 최소 축소 비율을 0.3으로 조정
    drawAdminMap();
}

function resetView() {
    scale = 1;
    offsetX = 0;
    offsetY = 0;
    fitCanvasToContainer();
    drawAdminMap();
}

/**
 * 상점 저장
 */
async function saveStore(e) {
    e.preventDefault();
    
    if (selectedX === null || selectedY === null) {
        showAlert('지도에서 위치를 먼저 선택해주세요.', 'error');
        return;
    }
    
    const formData = new FormData(document.getElementById('storeForm'));
    const storeId = document.getElementById('storeId').value;
    
    // storeId가 있으면 수정, 없으면 등록
    const isEdit = storeId && storeId.trim() !== '';
    const url = isEdit ? `/api/admin/stores/${storeId}` : '/api/admin/stores';
    const method = isEdit ? 'PUT' : 'POST';
    
    try {
        const response = await fetch(url, {
            method: method,
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            showAlert(isEdit ? '상점이 수정되었습니다.' : '상점이 등록되었습니다.', 'success');
            loadStores();
            resetForm();
        } else {
            showAlert(result.message || (isEdit ? '상점 수정에 실패했습니다.' : '상점 등록에 실패했습니다.'), 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('상점 저장 중 오류가 발생했습니다.', 'error');
    }
}

/**
 * 폼 초기화
 */
function resetForm() {
    document.getElementById('storeForm').reset();
    selectedX = null;
    selectedY = null;
    document.getElementById('xCoordDisplay').textContent = '-';
    document.getElementById('yCoordDisplay').textContent = '-';
    const tempMarker = document.getElementById('tempMarker');
    if (tempMarker) tempMarker.style.display = 'none';
    document.getElementById('imagePreview').innerHTML = '';
    drawAdminMap();
}

/**
 * 상점 목록 로드
 */
async function loadStores() {
    try {
        console.log('상점 목록 로드 시작');
        const response = await fetch('/api/admin/stores');
        console.log('API 응답 상태:', response.status);
        
        if (response.ok) {
            const result = await response.json();
            console.log('API 응답 데이터:', result);
            
            if (result.success) {
                const data = result.stores;
                stores = Array.isArray(data) ? data : [];
            } else {
                console.log('API 응답 실패:', result.message);
                stores = getDummyStores();
            }
        } else {
            console.log('API 응답 실패, 테스트 데이터 사용');
            stores = getDummyStores();
        }
    } catch (error) {
        console.error('상점 목록 로드 오류:', error);
        stores = getDummyStores();
    }
    
    console.log('최종 stores 배열:', stores);
    updateStoreTable();
    drawAdminMap();
}

/**
 * 테스트용 더미 데이터
 */
function getDummyStores() {
    return [
        {storeId: 1, storeName: '맛있는 김밥천국', storeCode: '1-A01', zoneNumber: 1, 
         phoneNumber: '031-123-4567', xCoordinate: 450, yCoordinate: 320},
        {storeId: 2, storeName: '스타벅스', storeCode: '2-B05', zoneNumber: 2, 
         phoneNumber: '031-234-5678', xCoordinate: 650, yCoordinate: 450}
    ];
}

/**
 * 상점 테이블 업데이트
 */
function updateStoreTable() {
    const tbody = document.getElementById('storeTableBody');
    if (!tbody) {
        console.error('storeTableBody 요소를 찾을 수 없습니다.');
        return;
    }
    
    console.log('상점 테이블 업데이트 시작, stores 개수:', stores.length);
    tbody.innerHTML = '';
    
    stores.forEach((store, index) => {
        console.log(`상점 ${index + 1}:`, store);
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${store.storeCode || ''}</td>
            <td>${store.storeName || ''}</td>
            <td>${store.zoneNumber || ''}지구</td>
            <td>${store.phoneNumber || ''}</td>
            <td>${store.xCoordinate || ''}, ${store.yCoordinate || ''}</td>
            <td><span class="badge bg-success">활성</span></td>
            <td>
                <button class="btn-edit" onclick="editStore(${store.storeId})">수정</button>
                <button class="btn-delete" onclick="deleteStore(${store.storeId})">삭제</button>
            </td>
        `;
        tbody.appendChild(row);
    });
    
    console.log('상점 테이블 업데이트 완료');
}


/**
 * 상점 수정
 */
async function editStore(storeId) {
    console.log('editStore 호출됨, storeId:', storeId);
    console.log('현재 stores 배열:', stores);
    
    const store = stores.find(s => s.storeId === storeId);
    if (!store) {
        console.error('상점을 찾을 수 없습니다. storeId:', storeId);
        showAlert('상점 정보를 찾을 수 없습니다.', 'error');
        return;
    }
    
    console.log('찾은 상점 정보:', store);
    
    // 폼에 데이터 채우기 (null 또는 "null" 문자열 처리)
    document.getElementById('storeId').value = store.storeId || '';
    document.getElementById('storeName').value = (store.storeName && store.storeName !== 'null') ? store.storeName : '';
    document.getElementById('storeCode').value = (store.storeCode && store.storeCode !== 'null') ? store.storeCode : '';
    document.getElementById('zoneNumber').value = (store.zoneNumber && store.zoneNumber !== 'null') ? store.zoneNumber : '1';
    document.getElementById('phoneNumber').value = (store.phoneNumber && store.phoneNumber !== 'null') ? store.phoneNumber : '';
    document.getElementById('businessHours').value = (store.businessHours && store.businessHours !== 'null') ? store.businessHours : '';
    document.getElementById('description').value = (store.description && store.description !== 'null') ? store.description : '';
    
    // 좌표 설정
    selectedX = store.xCoordinate;
    selectedY = store.yCoordinate;
    document.getElementById('xCoordDisplay').textContent = selectedX || '-';
    document.getElementById('yCoordDisplay').textContent = selectedY || '-';
    document.getElementById('xCoordinate').value = selectedX || '';
    document.getElementById('yCoordinate').value = selectedY || '';
    
    console.log('폼 데이터 설정 완료');
    console.log('좌표:', selectedX, selectedY);
    
    // 임시 마커 표시
    const tempMarker = document.getElementById('tempMarker');
    if (tempMarker && selectedX && selectedY) {
        tempMarker.style.display = 'block';
        // 마커 위치를 캔버스 좌표로 변환하여 표시
        const rect = canvas.getBoundingClientRect();
        const markerX = (selectedX * scale + offsetX) * (rect.width / canvas.width);
        const markerY = (selectedY * scale + offsetY) * (rect.height / canvas.height);
        tempMarker.style.left = markerX + 'px';
        tempMarker.style.top = markerY + 'px';
    }
    
    // 기존 이미지 로드 및 표시
    await loadExistingImages(storeId);
    
    drawAdminMap();
    
    // 스크롤 이동
    document.querySelector('.form-section').scrollIntoView({ behavior: 'smooth' });
    
    showAlert('상점 정보를 불러왔습니다. 위치를 변경하려면 지도를 클릭하세요.', 'info');
}

/**
 * 기존 이미지 로드 및 표시
 */
async function loadExistingImages(storeId) {
    const imagePreview = document.getElementById('imagePreview');
    if (!imagePreview) return;

    try {
        console.log('기존 이미지 로드 시작, storeId:', storeId);
        const response = await fetch(`/api/stores/${storeId}/images`);

        if (response.ok) {
            const images = await response.json();
            console.log('기존 이미지 데이터:', images);
            console.log('이미지 개수:', images.length);

            if (images && images.length > 0) {
                imagePreview.innerHTML = '';
                images.forEach((image, index) => {
                    const imageDiv = document.createElement('div');
                    imageDiv.className = 'existing-image-item';
                    imageDiv.style.cssText = `
                        display: inline-block;
                        margin: 5px;
                        position: relative;
                        border: 2px solid #ddd;
                        border-radius: 8px;
                        overflow: hidden;
                    `;

                    // 이미지 URL 처리 - 웹 접근 경로로 변환
                    let imageUrl = image.imageUrl;
                    console.log(`이미지 ${index + 1} 원본 URL:`, imageUrl);
                    if (imageUrl) {
                        // /uploads/stores/로 시작하는 경우 그대로 사용 (WebConfig에서 매핑됨)
                        if (imageUrl.startsWith('/uploads/stores/')) {
                            // 이미 올바른 경로이므로 그대로 사용
                            console.log(`이미지 ${index + 1} URL 유지:`, imageUrl);
                        } else if (!imageUrl.startsWith('http') && !imageUrl.startsWith('/')) {
                            // 파일명만 있는 경우 /uploads/stores/ 경로 추가
                            imageUrl = '/uploads/stores/' + imageUrl;
                            console.log(`이미지 ${index + 1} 변환된 URL:`, imageUrl);
                        }
                    }

                    imageDiv.innerHTML = `
                        <img src="${imageUrl}" alt="기존 이미지" style="width: 100px; height: 100px; object-fit: cover;"
                             onload="console.log('✅ 이미지 로드 성공:', '${imageUrl}')"
                             onerror="console.error('❌ 이미지 로드 실패:', '${imageUrl}'); console.log('HTTP 상태 확인을 위해 fetch로 테스트...'); 
                                      fetch('${imageUrl}').then(r => console.log('Fetch 결과:', r.status, r.statusText)).catch(e => console.error('Fetch 오류:', e));
                                      this.src='data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjRjVGNUY1IiBzdHJva2U9IiNDQ0MiIHN0cm9rZS13aWR0aD0iMiIvPgo8Y2lyY2xlIGN4PSI1MCIgY3k9IjM1IiByPSIxNSIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjOTk5IiBzdHJva2Utd2lkdGg9IjMiLz4KPHBhdGggZD0iTTMwIDY1TDUwIDQ1TDcwIDY1IiBzdHJva2U9IiM5OTkiIHN0cm9rZS13aWR0aD0iMyIgZmlsbD0ibm9uZSIvPgo8dGV4dCB4PSI1MCIgeT0iODAiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxMiIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+SW1hZ2U8L3RleHQ+Cjx0ZXh0IHg9IjUwIiB5PSI5NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjEwIiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj5Ob3QgRm91bmQ8L3RleHQ+Cjwvc3ZnPgo='; this.alt='이미지 로드 실패'; console.log('대체 이미지로 변경됨');">
                        <button class="existing-image-delete-btn" onclick="deleteExistingImage(${image.imageId})"
                                style="position: absolute; top: 5px; right: 5px; background: #f44336; color: white; border: none;
                                       border-radius: 50%; width: 24px; height: 24px; cursor: pointer; font-size: 16px;
                                       line-height: 20px; text-align: center; z-index: 10;">×</button>
                        <div style="position: absolute; top: 35px; right: 5px; background: rgba(0,0,0,0.7); color: white; padding: 2px 5px; font-size: 10px; border-radius: 3px;">
                            ${image.imageType === 'MAIN' ? '대표' : '추가'}
                        </div>
                        <div style="position: absolute; bottom: 0; left: 0; right: 0; background: rgba(0,0,0,0.7); color: white; padding: 2px; font-size: 10px; text-align: center;">
                            기존 이미지 ${index + 1}
                        </div>
                    `;

                    imagePreview.appendChild(imageDiv);
                });

                // 안내 메시지 추가
                const infoDiv = document.createElement('div');
                infoDiv.style.cssText = `
                    margin-top: 10px;
                    padding: 10px;
                    background: #e3f2fd;
                    border-radius: 5px;
                    font-size: 14px;
                    color: #1976d2;
                `;
                infoDiv.innerHTML = '💡 <strong>기존 이미지</strong><br>새 이미지를 추가하면 기존 이미지와 함께 저장됩니다. X 버튼을 클릭하여 이미지를 삭제할 수 있습니다.';
                imagePreview.appendChild(infoDiv);
            } else {
                imagePreview.innerHTML = '<div style="padding: 10px; color: #999; font-style: italic;">등록된 이미지가 없습니다.</div>';
            }
        } else {
            console.log('기존 이미지 로드 실패:', response.status);
            imagePreview.innerHTML = '<div style="padding: 10px; color: #f44336;">이미지를 불러올 수 없습니다.</div>';
        }
    } catch (error) {
        console.error('기존 이미지 로드 오류:', error);
        imagePreview.innerHTML = '<div style="padding: 10px; color: #f44336;">이미지 로드 중 오류가 발생했습니다.</div>';
    }
}

/**
 * 기존 이미지 삭제
 */
async function deleteExistingImage(imageId) {
    if (!confirm('이 이미지를 삭제하시겠습니까?')) return;

    try {
        console.log('이미지 삭제 시작, imageId:', imageId);
        const response = await fetch(`/api/admin/images/${imageId}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (result.success) {
            showAlert('이미지가 삭제되었습니다.', 'success');
            // 현재 편집 중인 상점 ID 가져오기
            const storeId = document.getElementById('storeId').value;
            if (storeId) {
                // 이미지 목록 다시 로드
                await loadExistingImages(storeId);
            }
        } else {
            showAlert(result.message || '이미지 삭제에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('이미지 삭제 오류:', error);
        showAlert('이미지 삭제 중 오류가 발생했습니다.', 'error');
    }
}

/**
 * 상점 삭제
 */
async function deleteStore(storeId) {
    if (!confirm('정말로 삭제하시겠습니까?')) return;
    
    try {
        const response = await fetch(`/api/admin/stores/${storeId}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (result.success) {
            stores = stores.filter(s => s.storeId !== storeId);
            updateStoreTable();
            drawAdminMap();
            showAlert('상점이 삭제되었습니다.', 'success');
        } else {
            showAlert(result.message || '상점 삭제에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('상점 삭제 중 오류가 발생했습니다.', 'error');
    }
}

/**
 * 이미지 미리보기
 */
function previewImages(input) {
    const preview = document.getElementById('imagePreview');
    if (!preview) return;
    
    preview.innerHTML = '';
    
    if (input.files) {
        Array.from(input.files).forEach(file => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const div = document.createElement('div');
                div.className = 'image-preview-item';
                div.innerHTML = `
                    <img src="${e.target.result}" alt="Preview">
                    <button class="image-remove" onclick="this.parentElement.remove()">×</button>
                `;
                preview.appendChild(div);
            };
            reader.readAsDataURL(file);
        });
    }
}

/**
 * 알림 메시지
 */
function showAlert(message, type) {
    const alert = document.getElementById('alertMessage');
    if (!alert) return;
    
    alert.className = `alert-custom alert-${type}`;
    alert.textContent = message;
    alert.style.display = 'block';
    
    setTimeout(() => {
        alert.style.display = 'none';
    }, 3000);
}

/**
 * 로그아웃
 */
async function logout() {
    if (!confirm('로그아웃 하시겠습니까?')) return;
    
    try {
        const response = await fetch('/api/admin/logout', {
            method: 'POST'
        });
        
        if (response.ok) {
            location.href = '/admin/login';
        } else {
            showAlert('로그아웃 중 오류가 발생했습니다.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        location.href = '/admin/login';
    }
}
