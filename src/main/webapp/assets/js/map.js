/**
 * 관문상가시장 지도 서비스 JavaScript
 */

// 전역 변수
let canvas, ctx;
let mapImage = new Image();
let stores = [];
let selectedZone = 0;
let scale = 1;
let offsetX = 0, offsetY = 0;
let isDragging = false;
let dragStartX, dragStartY;

/**
 * 페이지 로드 시 초기화
 */
window.addEventListener('DOMContentLoaded', function() {
    initializeMap();
    loadStores();
});

/**
 * 지도 초기화
 */
function initializeMap() {
    canvas = document.getElementById('mapCanvas');
    if (!canvas) return;
    
    ctx = canvas.getContext('2d');
    
    // 지도 이미지 로드
    mapImage.src = '/static/images/map.png';
    mapImage.onload = function() {
        // 캔버스 크기 설정
        canvas.width = mapImage.width;
        canvas.height = mapImage.height;
        
        // 반응형 크기 조정
        fitCanvasToContainer();
        
        // 지도 그리기
        drawMap();
    };
    
    // 이벤트 리스너
    canvas.addEventListener('click', handleMapClick);
    canvas.addEventListener('mousedown', startDrag);
    canvas.addEventListener('mousemove', drag);
    canvas.addEventListener('mouseup', endDrag);
    canvas.addEventListener('wheel', handleZoom);
    
    // 리사이즈 이벤트
    window.addEventListener('resize', fitCanvasToContainer);
}

/**
 * 캔버스 크기 조정
 */
function fitCanvasToContainer() {
    const container = document.querySelector('.map-canvas-container');
    if (!container) return;
    
    // 컨테이너의 가용 너비 계산
    const containerWidth = container.clientWidth - 4; // 패딩 제외
    const containerHeight = window.innerHeight * 0.7;
    
    const aspectRatio = mapImage.width / mapImage.height;
    
    // 가로폭을 우선시하여 조정
    let newWidth = containerWidth;
    let newHeight = newWidth / aspectRatio;
    
    // 높이가 최대 높이를 초과해도 가로폭을 우선시
    // 스크롤로 처리하도록 함
    
    // 스케일 계산
    scale = newWidth / mapImage.width;
    
    // 캔버스 표시 크기 설정
    canvas.style.width = newWidth + 'px';
    canvas.style.height = newHeight + 'px';
    
    // 캔버스 실제 크기 설정 (고해상도 디스플레이 대응)
    const devicePixelRatio = window.devicePixelRatio || 1;
    canvas.width = newWidth * devicePixelRatio;
    canvas.height = newHeight * devicePixelRatio;
    
    // 컨텍스트 스케일 조정
    ctx.scale(devicePixelRatio, devicePixelRatio);
    
    // 오프셋 초기화하여 이미지가 캔버스 중앙에 위치하도록 함
    offsetX = 0;
    offsetY = 0;
    
    drawMap();
}

/**
 * 지도 그리기
 */
function drawMap() {
    if (!ctx || !mapImage) return;
    
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.save();
    ctx.translate(offsetX, offsetY);
    ctx.scale(scale, scale);
    ctx.drawImage(mapImage, 0, 0);
    
    // 상점 마커 그리기
    drawStoreMarkers();
    
    ctx.restore();
}

/**
 * 상점 마커 그리기
 */
function drawStoreMarkers() {
    if (!ctx) return;
    
    stores.forEach(store => {
        if (selectedZone === 0 || store.zoneNumber === selectedZone) {
            drawMarker(store.xCoordinate, store.yCoordinate, store.categoryId, store.storeName);
        }
    });
}

/**
 * 마커 그리기
 */
function drawMarker(x, y, categoryId, name) {
    if (!ctx) return;
    
    ctx.save();
    
    // 마커 색상 설정
    const colors = {
        1: '#ff6b6b', // 음식점
        2: '#4ecdc4', // 카페
        3: '#45b7d1', // 편의점
        4: '#96ceb4', // 병원/약국
        5: '#feca57'  // 미용실
    };
    
    ctx.fillStyle = colors[categoryId] || '#999';
    
    // 원형 마커 그리기
    ctx.beginPath();
    ctx.arc(x, y, 15, 0, 2 * Math.PI);
    ctx.fill();
    
    // 마커 중앙에 점
    ctx.fillStyle = 'white';
    ctx.beginPath();
    ctx.arc(x, y, 5, 0, 2 * Math.PI);
    ctx.fill();
    
    // 상점명 표시 (작게)
    ctx.fillStyle = '#333';
    ctx.font = '12px Arial';
    ctx.textAlign = 'center';
    ctx.fillText(name, x, y - 20);
    
    ctx.restore();
}

/**
 * 상점 데이터 로드
 */
async function loadStores() {
    try {
        const response = await fetch('/api/stores/list');
        if (response.ok) {
            stores = await response.json();
        } else {
            console.error('상점 데이터 로드 실패');
            // 테스트용 더미 데이터
            stores = getDummyStores();
        }
    } catch (error) {
        console.error('Error:', error);
        // 테스트용 더미 데이터
        stores = getDummyStores();
    }
    
    updateStoreList();
    drawMap();
}

/**
 * 테스트용 더미 데이터
 */
function getDummyStores() {
    return [
        {storeId: 1, storeName: '맛있는 김밥천국', storeCode: '1-A01', zoneNumber: 1, categoryId: 1, 
         phoneNumber: '031-123-4567', xCoordinate: 450, yCoordinate: 320, 
         businessHours: '09:00-21:00', description: '다양한 분식 메뉴'},
        {storeId: 2, storeName: '스타벅스', storeCode: '2-B05', zoneNumber: 2, categoryId: 2, 
         phoneNumber: '031-234-5678', xCoordinate: 650, yCoordinate: 450,
         businessHours: '07:00-23:00', description: '커피 전문점'},
        {storeId: 3, storeName: 'GS25', storeCode: '3-C10', zoneNumber: 3, categoryId: 3, 
         phoneNumber: '031-345-6789', xCoordinate: 320, yCoordinate: 550,
         businessHours: '24시간', description: '편의점'}
    ];
}

/**
 * 상점 목록 업데이트
 */
function updateStoreList() {
    const listContainer = document.getElementById('storeList');
    if (!listContainer) return;
    
    listContainer.innerHTML = '';
    
    const filteredStores = selectedZone === 0 
        ? stores 
        : stores.filter(s => s.zoneNumber === selectedZone);
    
    filteredStores.forEach(store => {
        const item = document.createElement('div');
        item.className = 'store-item';
        item.onclick = () => showStoreDetail(store);
        item.innerHTML = `
            <div class="store-name">${store.storeName}</div>
            <div class="store-info">${store.zoneNumber}지구 | ${store.phoneNumber || '전화번호 없음'}</div>
        `;
        listContainer.appendChild(item);
    });
}

/**
 * 구역 필터
 */
function filterByZone(zone) {
    selectedZone = zone;
    
    // 탭 활성화 상태 업데이트
    document.querySelectorAll('.zone-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    event.target.classList.add('active');
    
    updateStoreList();
    drawMap();
}

/**
 * 지도 클릭 처리
 */
function handleMapClick(e) {
    if (!canvas) return;
    
    const rect = canvas.getBoundingClientRect();
    const x = (e.clientX - rect.left) * (canvas.width / rect.width) / scale - offsetX / scale;
    const y = (e.clientY - rect.top) * (canvas.height / rect.height) / scale - offsetY / scale;
    
    // 클릭된 상점 찾기
    const clickedStore = stores.find(store => {
        const distance = Math.sqrt(
            Math.pow(store.xCoordinate - x, 2) + 
            Math.pow(store.yCoordinate - y, 2)
        );
        return distance <= 20; // 클릭 반경
    });
    
    if (clickedStore) {
        showStoreDetail(clickedStore);
    }
}

/**
 * 상점 상세 정보 표시
 */
function showStoreDetail(store) {
    const popup = document.getElementById('storePopup');
    if (!popup) return;
    
    document.getElementById('popupTitle').textContent = store.storeName;
    document.getElementById('popupPhone').textContent = store.phoneNumber || '정보 없음';
    document.getElementById('popupZone').textContent = `${store.zoneNumber || '?'}지구 (${store.storeCode || '코드 없음'})`;
    document.getElementById('popupHours').textContent = store.businessHours || '정보 없음';
    document.getElementById('popupDescription').textContent = store.description || '설명 없음';
    
    // 상점 이미지 로드
    loadStoreImages(store.storeId);
    
    // 팝업 표시
    document.querySelector('.popup-overlay').style.display = 'block';
    popup.style.display = 'block';
    
    // 지도에서 해당 상점으로 포커스
    focusOnStore(store);
}

/**
 * 상점 이미지 로드
 */
async function loadStoreImages(storeId) {
    const popupImages = document.getElementById('popupImages');
    if (!popupImages) return;
    
    // 이미지 컨테이너 초기화
    popupImages.innerHTML = '';
    
    try {
        console.log('상점 이미지 로드 시작, storeId:', storeId);
        const response = await fetch(`/api/stores/${storeId}/images`);
        
        if (response.ok) {
            const images = await response.json();
            console.log('이미지 데이터:', images);
            
            if (images && images.length > 0) {
                images.forEach(image => {
                    const imageDiv = document.createElement('div');
                    imageDiv.className = 'popup-image-item';
                    
                    // 이미지 URL이 상대 경로인 경우 절대 경로로 변환
                    let imageUrl = image.imageUrl;
                    if (imageUrl && !imageUrl.startsWith('http') && !imageUrl.startsWith('/')) {
                        imageUrl = '/' + imageUrl;
                    }
                    
                    imageDiv.innerHTML = `
                        <img src="${imageUrl}" alt="상점 이미지" 
                             onload="console.log('사용자 페이지 이미지 로드 성공:', '${imageUrl}')" 
                             onerror="console.error('사용자 페이지 이미지 로드 실패:', '${imageUrl}'); this.style.display='none'">
                        <div class="image-caption">${image.imageType === 'MAIN' ? '대표 이미지' : '추가 이미지'}</div>
                    `;
                    
                    popupImages.appendChild(imageDiv);
                });
            } else {
                popupImages.innerHTML = '<div class="no-images">등록된 이미지가 없습니다.</div>';
            }
        } else {
            console.log('이미지 로드 실패:', response.status);
            popupImages.innerHTML = '<div class="no-images">이미지를 불러올 수 없습니다.</div>';
        }
    } catch (error) {
        console.error('이미지 로드 오류:', error);
        popupImages.innerHTML = '<div class="no-images">이미지 로드 중 오류가 발생했습니다.</div>';
    }
}

/**
 * 특정 상점으로 포커스
 */
function focusOnStore(store) {
    if (!canvas) return;
    
    // 상점 위치를 중앙으로 이동
    const container = document.querySelector('.map-canvas-container');
    if (!container) return;
    
    const centerX = container.offsetWidth / 2;
    const centerY = container.offsetHeight / 2;
    
    offsetX = centerX - store.xCoordinate * scale;
    offsetY = centerY - store.yCoordinate * scale;
    
    drawMap();
    
    // 하이라이트 효과
    setTimeout(() => {
        if (!ctx) return;
        ctx.save();
        ctx.strokeStyle = '#ff0000';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.arc(store.xCoordinate + offsetX, store.yCoordinate + offsetY, 25, 0, 2 * Math.PI);
        ctx.stroke();
        ctx.restore();
    }, 100);
}

/**
 * 팝업 닫기
 */
function closePopup() {
    document.querySelector('.popup-overlay').style.display = 'none';
    const popup = document.getElementById('storePopup');
    if (popup) {
        popup.style.display = 'none';
    }
}

/**
 * 검색 기능
 */
async function searchStore() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;
    
    const keyword = searchInput.value.toLowerCase();
    
    if (!keyword) {
        loadStores();
        return;
    }
    
    try {
        const response = await fetch(`/api/stores/search?keyword=${encodeURIComponent(keyword)}`);
        if (response.ok) {
            stores = await response.json();
        } else {
            // 로컬 검색
            stores = stores.filter(store => 
                store.storeName.toLowerCase().includes(keyword) ||
                store.storeCode.toLowerCase().includes(keyword)
            );
        }
    } catch (error) {
        console.error('Search error:', error);
        // 로컬 검색
        stores = stores.filter(store => 
            store.storeName.toLowerCase().includes(keyword) ||
            store.storeCode.toLowerCase().includes(keyword)
        );
    }
    
    updateStoreList();
    drawMap();
    
    if (stores.length === 1) {
        showStoreDetail(stores[0]);
    }
}

/**
 * 검색 초기화 기능
 */
function resetSearch() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
    }
    
    // 모든 상점 다시 로드
    loadStores();
    
    // 구역 필터도 초기화
    selectedZone = 0;
    document.querySelectorAll('.zone-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 지도 뷰 초기화
    scale = 1;
    offsetX = 0;
    offsetY = 0;
    fitCanvasToContainer();
}

/**
 * 드래그 기능
 */
function startDrag(e) {
    isDragging = true;
    dragStartX = e.clientX - offsetX;
    dragStartY = e.clientY - offsetY;
    canvas.style.cursor = 'grabbing';
}

function drag(e) {
    if (!isDragging) return;
    
    e.preventDefault();
    offsetX = e.clientX - dragStartX;
    offsetY = e.clientY - dragStartY;
    drawMap();
}

function endDrag() {
    isDragging = false;
    canvas.style.cursor = 'grab';
}

/**
 * 줌 기능
 */
function handleZoom(e) {
    e.preventDefault();
    
    const delta = e.deltaY > 0 ? 0.9 : 1.1;
    const newScale = scale * delta;
    
    // 줌 제한
    if (newScale < 0.5 || newScale > 3) return;
    
    scale = newScale;
    drawMap();
}

/**
 * 엔터키 검색 이벤트
 */
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchStore();
            }
        });
    }
});
