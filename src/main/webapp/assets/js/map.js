/**
 * 관문상가시장 지도 서비스 JavaScript
 */

// 전역 변수
let canvas, ctx;
let mapImage = new Image();
let stores = [];
let allStores = []; // 모든 상점 데이터 (원본 보관용)
let selectedZone = 0;
let baseScale = 1; // 초기 캔버스 크기에 맞춘 기본 스케일
let userScale = 1; // 사용자가 줌으로 조정한 스케일
let offsetX = 0, offsetY = 0;
let isDragging = false;
let dragStartX, dragStartY;
let highlightedStores = []; // 하이라이트된 상점 ID 배열 (클릭/검색)
let hoveredStoreId = null; // 마우스 호버 중인 상점 ID

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
            console.log('이미지 로딩 시도:', imagePaths[currentPathIndex]);
        }
    }
    
    tryLoadImage();
    mapImage.onload = function() {
        console.log('지도 이미지 로드 성공:', mapImage.src);
        console.log('이미지 크기:', mapImage.width, 'x', mapImage.height);
        
        // 캔버스 크기 설정
        canvas.width = mapImage.width;
        canvas.height = mapImage.height;
        
        // 반응형 크기 조정
        fitCanvasToContainer();
        
        // 지도 그리기
        drawMap();
    };
    
    mapImage.onerror = function() {
        console.error('지도 이미지 로드 실패:', mapImage.src);
        
        // 다음 경로 시도
        currentPathIndex++;
        if (currentPathIndex < imagePaths.length) {
            console.log('다음 경로로 재시도:', imagePaths[currentPathIndex]);
            tryLoadImage();
        } else {
            console.error('모든 경로에서 이미지 로드 실패');
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
    canvas.addEventListener('mousedown', startDrag);
    canvas.addEventListener('mousemove', handleMouseMove);
    canvas.addEventListener('mouseup', endDrag);
    canvas.addEventListener('mouseleave', handleMouseLeave);
    canvas.addEventListener('wheel', handleZoom);
    
    // 모바일 터치 이벤트 - 확대/축소와 클릭 모두 지원
    canvas.addEventListener('touchstart', handleTouchStart, { passive: false });
    canvas.addEventListener('touchmove', handleTouchMove, { passive: false });
    canvas.addEventListener('touchend', handleTouchEnd, { passive: false });
    
    // 리사이즈 이벤트
    window.addEventListener('resize', fitCanvasToContainer);
    
    // 애니메이션 루프 시작
    startAnimationLoop();
}

/**
 * 애니메이션 루프
 */
function startAnimationLoop() {
    function animate() {
        drawMap(); // 마커 애니메이션을 위해 지도를 다시 그림
        requestAnimationFrame(animate);
    }
    animate();
}

/**
 * 캔버스 크기 조정
 */
function fitCanvasToContainer() {
    const container = document.querySelector('.map-canvas-container');
    if (!container) return;
    
    // 고해상도 디스플레이 대응
    const devicePixelRatio = window.devicePixelRatio || 1;
    
    // 컨테이너의 가용 너비 계산
    const containerWidth = container.clientWidth - 4; // 패딩 제외
    const containerHeight = window.innerHeight * 0.7;
    
    const aspectRatio = mapImage.width / mapImage.height;
    
    // 가로폭을 우선시하여 조정
    let newWidth = containerWidth;
    let newHeight = newWidth / aspectRatio;
    
    // 높이가 최대 높이를 초과해도 가로폭을 우선시
    // 스크롤로 처리하도록 함
    
    // 기본 스케일 계산 (캔버스 크기에 맞추기 위한 스케일)
    baseScale = newWidth / mapImage.width;
    
    // 캔버스 표시 크기 설정
    canvas.style.width = newWidth + 'px';
    canvas.style.height = newHeight + 'px';
    
    // 캔버스 실제 크기 설정 (고해상도 대응)
    canvas.width = newWidth * devicePixelRatio;
    canvas.height = newHeight * devicePixelRatio;
    
    // 컨텍스트 스케일 조정
    ctx.scale(devicePixelRatio, devicePixelRatio);
    
    // 오프셋 초기화
    offsetX = 0;
    offsetY = 0;
    
    console.log(`캔버스 크기 조정: 표시=${newWidth}x${newHeight}, 실제=${canvas.width}x${canvas.height}, DPR=${devicePixelRatio}`);
    
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
    
    // 기본 스케일 * 사용자 줌 스케일
    const totalScale = baseScale * userScale;
    ctx.scale(totalScale, totalScale);
    
    // 이미지 렌더링 품질 향상
    ctx.imageSmoothingEnabled = true;
    ctx.imageSmoothingQuality = 'high';
    
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
            // 하이라이트 조건: 클릭/검색된 상점 또는 호버 중인 상점
            const isHighlighted = highlightedStores.includes(store.storeId) || hoveredStoreId === store.storeId;
            drawMarker(store.xCoordinate, store.yCoordinate, store.categoryId, store.storeName, isHighlighted);
        }
    });
}

/**
 * 마커 그리기
 */
function drawMarker(x, y, categoryId, name, isHighlighted = false) {
    if (!ctx) return;
    
    ctx.save();
    
    // 마커 색상 설정 (더 밝고 대비가 강한 색상)
    const colors = {
        1: '#ff4757', // 음식점 - 빨간색
        2: '#2ed573', // 카페 - 초록색
        3: '#3742fa', // 편의점 - 파란색
        4: '#ffa502', // 병원/약국 - 주황색
        5: '#ff6348'  // 미용실 - 핑크색
    };
    
    const markerColor = colors[categoryId] || '#ff4757'; // 기본값도 빨간색으로
    
    // 하이라이트되지 않은 경우 투명하게 표시
    if (!isHighlighted) {
        // 투명한 마커 (클릭 영역만 유지하기 위해 매우 투명하게)
        ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
        ctx.beginPath();
        ctx.arc(x, y, 20, 0, 2 * Math.PI);
        ctx.fill();
        ctx.restore();
        return;
    }
    
    // 하이라이트된 경우: 빨간색으로 강조 표시
    const highlightColor = '#ff4757'; // 빨간색으로 고정
    
    // 펄스 효과 (애니메이션)
    const time = Date.now() * 0.003;
    const pulseScale = 1 + Math.sin(time) * 0.1;
    
    // 펄스 효과 배경 (반투명)
    ctx.fillStyle = highlightColor + '40'; // 40은 투명도
    ctx.beginPath();
    ctx.arc(x, y, 25 * pulseScale, 0, 2 * Math.PI);
    ctx.fill();
    
    // 외곽 테두리 (검은색)
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.arc(x, y, 20, 0, 2 * Math.PI);
    ctx.stroke();
    
    // 메인 마커 (빨간색)
    ctx.fillStyle = highlightColor;
    ctx.beginPath();
    ctx.arc(x, y, 20, 0, 2 * Math.PI);
    ctx.fill();
    
    // 내부 테두리 (흰색)
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(x, y, 20, 0, 2 * Math.PI);
    ctx.stroke();
    
    // 마커 중앙에 점 (흰색)
    ctx.fillStyle = '#ffffff';
    ctx.beginPath();
    ctx.arc(x, y, 7, 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.restore();
}

/**
 * 상점 데이터 로드
 */
async function loadStores() {
    try {
        const response = await fetch('/api/stores/list');
        if (response.ok) {
            allStores = await response.json();
            stores = [...allStores]; // 복사본 생성
        } else {
            console.error('상점 데이터 로드 실패');
            // 테스트용 더미 데이터
            allStores = getDummyStores();
            stores = [...allStores]; // 복사본 생성
        }
    } catch (error) {
        console.error('Error:', error);
        // 테스트용 더미 데이터
        allStores = getDummyStores();
        stores = [...allStores]; // 복사본 생성
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
    const totalScale = baseScale * userScale;

    // 화면 좌표를 표시된 캔버스 좌표로 변환 (DPR 고려하지 않음)
    const displayX = e.clientX - rect.left;
    const displayY = e.clientY - rect.top;

    // 오프셋과 스케일을 역으로 적용하여 원본 이미지 좌표로 변환
    const imageX = (displayX - offsetX) / totalScale;
    const imageY = (displayY - offsetY) / totalScale;

    // 클릭된 상점 찾기
    const clickedStore = stores.find(store => {
        const distance = Math.sqrt(
            Math.pow(store.xCoordinate - imageX, 2) +
            Math.pow(store.yCoordinate - imageY, 2)
        );
        return distance <= 60; // 클릭 반경
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
    
    // 클릭한 상점만 하이라이트 (이전 클릭한 상점들의 마킹 제거)
    highlightedStores = [store.storeId];
    
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
    
    // 지도 다시 그리기 (마커 색상 업데이트)
    drawMap();
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
 * 특정 상점으로 포커스 (확대 및 중앙 이동)
 */
function focusOnStore(store) {
    if (!canvas) return;

    const container = document.querySelector('.map-canvas-container');
    if (!container) return;

    // 줌 레벨을 2배로 설정 (더 확대해서 보기)
    const targetUserScale = 2.0;

    // 줌 제한 확인
    if (targetUserScale < 0.5 || targetUserScale > 3) {
        return;
    }

    // 부드러운 애니메이션을 위한 변수
    const startScale = userScale;
    const startOffsetX = offsetX;
    const startOffsetY = offsetY;
    const duration = 500; // 애니메이션 시간 (밀리초)
    const startTime = Date.now();

    function animate() {
        const currentTime = Date.now();
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1); // 0 ~ 1

        // easeInOutQuad 이징 함수 (부드러운 가속/감속)
        const eased = progress < 0.5
            ? 2 * progress * progress
            : 1 - Math.pow(-2 * progress + 2, 2) / 2;

        // 현재 스케일 계산
        userScale = startScale + (targetUserScale - startScale) * eased;

        // 상점 위치를 중앙으로 이동하기 위한 오프셋 계산
        const centerX = container.offsetWidth / 2;
        const centerY = container.offsetHeight / 2;
        const totalScale = baseScale * userScale;

        const targetOffsetX = centerX - store.xCoordinate * totalScale;
        const targetOffsetY = centerY - store.yCoordinate * totalScale;

        // 부드러운 이동
        offsetX = startOffsetX + (targetOffsetX - startOffsetX) * eased;
        offsetY = startOffsetY + (targetOffsetY - startOffsetY) * eased;

        drawMap();

        // 애니메이션 계속 또는 종료
        if (progress < 1) {
            requestAnimationFrame(animate);
        } else {
            // 애니메이션 완료 후 하이라이트 효과
            highlightStore(store);
        }
    }

    // 애니메이션 시작
    animate();
}

/**
 * 상점 하이라이트 효과
 */
function highlightStore(store) {
    let pulseCount = 0;
    const maxPulses = 3; // 펄스 효과 횟수
    const pulseInterval = 300; // 펄스 간격

    function pulse() {
        if (pulseCount >= maxPulses) return;

        // 현재 변환된 좌표로 하이라이트 그리기
        const totalScale = baseScale * userScale;
        const highlightX = store.xCoordinate * totalScale + offsetX;
        const highlightY = store.yCoordinate * totalScale + offsetY;

        // 하이라이트 링 그리기
        ctx.save();
        ctx.strokeStyle = '#ff0000';
        ctx.lineWidth = 4;
        ctx.shadowColor = '#ff0000';
        ctx.shadowBlur = 10;
        ctx.beginPath();
        ctx.arc(highlightX, highlightY, 30, 0, 2 * Math.PI);
        ctx.stroke();
        ctx.restore();

        pulseCount++;

        if (pulseCount < maxPulses) {
            setTimeout(pulse, pulseInterval);
        }
    }

    // 첫 펄스 시작
    setTimeout(pulse, 100);
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
    
    const keyword = searchInput.value.toLowerCase().trim();
    
    if (!keyword) {
        // 검색어가 없으면 모든 상점 표시
        stores = [...allStores];
        highlightedStores = [];
        updateStoreList();
        drawMap();
        return;
    }
    
    try {
        const response = await fetch(`/api/stores/search?keyword=${encodeURIComponent(keyword)}`);
        if (response.ok) {
            stores = await response.json();
        } else {
            // 로컬 검색 (원본 데이터에서 검색)
            stores = allStores.filter(store => 
                store.storeName.toLowerCase().includes(keyword) ||
                (store.storeCode && store.storeCode.toLowerCase().includes(keyword))
            );
        }
    } catch (error) {
        console.error('Search error:', error);
        // 로컬 검색 (원본 데이터에서 검색)
        stores = allStores.filter(store => 
            store.storeName.toLowerCase().includes(keyword) ||
            (store.storeCode && store.storeCode.toLowerCase().includes(keyword))
        );
    }
    
    // 검색된 상점들만 하이라이트
    highlightedStores = stores.map(store => store.storeId);
    
    updateStoreList();
    drawMap();
    
    // 검색 결과가 1개면 자동으로 상세 정보 표시
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
    
    // 하이라이트 초기화
    highlightedStores = [];
    
    // 모든 상점 복원
    stores = [...allStores];
    
    // 구역 필터도 초기화
    selectedZone = 0;
    document.querySelectorAll('.zone-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 첫 번째 탭 활성화 (전체)
    const firstTab = document.querySelector('.zone-tab');
    if (firstTab) {
        firstTab.classList.add('active');
    }
    
    // 상점 목록 업데이트
    updateStoreList();
    
    // 지도 뷰 초기화
    userScale = 1;
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

function endDrag() {
    isDragging = false;
    canvas.style.cursor = 'grab';
}

/**
 * 마우스 이동 처리 (드래그 + 호버)
 */
function handleMouseMove(e) {
    // 드래그 처리
    if (isDragging) {
        e.preventDefault();
        offsetX = e.clientX - dragStartX;
        offsetY = e.clientY - dragStartY;
        drawMap();
        return;
    }
    
    // 호버 처리
    const rect = canvas.getBoundingClientRect();
    const totalScale = baseScale * userScale;
    
    // 화면 좌표를 표시된 캔버스 좌표로 변환
    const displayX = e.clientX - rect.left;
    const displayY = e.clientY - rect.top;
    
    // 오프셋과 스케일을 역으로 적용하여 원본 이미지 좌표로 변환
    const imageX = (displayX - offsetX) / totalScale;
    const imageY = (displayY - offsetY) / totalScale;
    
    // 마우스 위치에 있는 상점 찾기
    const hoveredStore = stores.find(store => {
        if (selectedZone !== 0 && store.zoneNumber !== selectedZone) {
            return false;
        }
        const distance = Math.sqrt(
            Math.pow(store.xCoordinate - imageX, 2) +
            Math.pow(store.yCoordinate - imageY, 2)
        );
        return distance <= 60; // 호버 반경
    });
    
    // 호버 상태 변경 확인
    const newHoveredId = hoveredStore ? hoveredStore.storeId : null;
    if (newHoveredId !== hoveredStoreId) {
        hoveredStoreId = newHoveredId;
        // 커서 스타일 변경
        canvas.style.cursor = hoveredStoreId ? 'pointer' : 'grab';
        // 지도 다시 그리기는 애니메이션 루프에서 처리되므로 별도 호출 불필요
    }
}

/**
 * 마우스가 캔버스를 벗어났을 때
 */
function handleMouseLeave() {
    if (hoveredStoreId !== null) {
        hoveredStoreId = null;
        canvas.style.cursor = 'grab';
    }
}

/**
 * 줌 기능
 */
function handleZoom(e) {
    e.preventDefault();
    
    const delta = e.deltaY > 0 ? 0.9 : 1.1;
    const newUserScale = userScale * delta;
    
    // 줌 제한
    if (newUserScale < 0.5 || newUserScale > 3) return;
    
    userScale = newUserScale;
    drawMap();
}

/**
 * 모바일 터치 이벤트 처리 - 확대/축소와 클릭 모두 지원
 */
let touchStartTime = 0;
let touchStartPos = { x: 0, y: 0 };
let isTouchMove = false;
let lastTouchDistance = 0;
let lastUserScale = 1;
let initialTouchDistance = 0;

function handleTouchStart(e) {
    e.preventDefault();
    
    if (e.touches.length === 1) {
        // 단일 터치 - 클릭 또는 드래그
        const touch = e.touches[0];
        touchStartTime = Date.now();
        touchStartPos = { x: touch.clientX, y: touch.clientY };
        isTouchMove = false;
        isDragging = false;
        dragStartX = 0;
        dragStartY = 0;
        
        console.log('모바일 터치 시작:', { x: touch.clientX, y: touch.clientY });
        
    } else if (e.touches.length === 2) {
        // 핀치 줌 시작
        isTouchMove = true;
        const touch1 = e.touches[0];
        const touch2 = e.touches[1];
        initialTouchDistance = Math.sqrt(
            Math.pow(touch2.clientX - touch1.clientX, 2) + 
            Math.pow(touch2.clientY - touch1.clientY, 2)
        );
        lastTouchDistance = initialTouchDistance;
        lastUserScale = userScale;
        
        console.log('핀치 줌 시작:', { initialDistance: initialTouchDistance });
    }
}

function handleTouchMove(e) {
    e.preventDefault();
    
    if (e.touches.length === 1) {
        // 단일 터치 이동 - 드래그
        const touch = e.touches[0];
        const moveDistance = Math.sqrt(
            Math.pow(touch.clientX - touchStartPos.x, 2) + 
            Math.pow(touch.clientY - touchStartPos.y, 2)
        );
        
        // 10px 이상 이동하면 드래그로 간주
        if (moveDistance > 10) {
            isTouchMove = true;
            
            if (!isDragging) {
                isDragging = true;
                dragStartX = touch.clientX - offsetX;
                dragStartY = touch.clientY - offsetY;
            }
            
            // 지도 이동
            offsetX = touch.clientX - dragStartX;
            offsetY = touch.clientY - dragStartY;
            drawMap();
        }
        
    } else if (e.touches.length === 2) {
        // 핀치 줌 처리
        isTouchMove = true;
        const touch1 = e.touches[0];
        const touch2 = e.touches[1];
        const currentDistance = Math.sqrt(
            Math.pow(touch2.clientX - touch1.clientX, 2) + 
            Math.pow(touch2.clientY - touch1.clientY, 2)
        );
        
        if (lastTouchDistance > 0) {
            // 거리 비율을 이용한 스케일 계산
            const delta = currentDistance / lastTouchDistance;
            const newUserScale = lastUserScale * delta;
            
            // 줌 제한
            if (newUserScale >= 0.5 && newUserScale <= 3) {
                userScale = newUserScale;
                drawMap();
            }
        }
        
        // 다음 프레임을 위해 현재 거리와 스케일 저장
        lastTouchDistance = currentDistance;
        lastUserScale = userScale;
    }
}

function handleTouchEnd(e) {
    e.preventDefault();
    const touchDuration = Date.now() - touchStartTime;

    console.log('모바일 터치 종료:', {
        touchDuration,
        isTouchMove,
        touches: e.changedTouches.length
    });

    // 드래그 변수 초기화
    dragStartX = 0;
    dragStartY = 0;
    isDragging = false;
    lastTouchDistance = 0;

    // 클릭 처리 (짧은 터치이고 이동하지 않았을 때)
    if (e.changedTouches.length > 0 && !isTouchMove && touchDuration < 500) {
        const touch = e.changedTouches[0];
        const rect = canvas.getBoundingClientRect();

        // 전체 스케일 계산
        const totalScale = baseScale * userScale;

        // 화면 좌표를 표시된 캔버스 좌표로 변환 (DPR 고려하지 않음)
        const displayX = touch.clientX - rect.left;
        const displayY = touch.clientY - rect.top;

        // 오프셋과 스케일을 역으로 적용하여 원본 이미지 좌표로 변환
        const imageX = (displayX - offsetX) / totalScale;
        const imageY = (displayY - offsetY) / totalScale;

        console.log('모바일 클릭 좌표 변환:', {
            client: { x: touch.clientX, y: touch.clientY },
            rect: { left: rect.left, top: rect.top },
            display: { x: displayX, y: displayY },
            image: { x: imageX, y: imageY },
            totalScale: totalScale,
            offset: { x: offsetX, y: offsetY }
        });

        // 클릭된 상점 찾기
        const clickedStore = stores.find(store => {
            const distance = Math.sqrt(
                Math.pow(store.xCoordinate - imageX, 2) +
                Math.pow(store.yCoordinate - imageY, 2)
            );
            console.log(`상점 ${store.storeName}: 거리=${distance.toFixed(2)}, 좌표=(${store.xCoordinate}, ${store.yCoordinate})`);
            return distance <= 100; // 클릭 반경
        });

        if (clickedStore) {
            console.log('✓ 모바일에서 상점 선택:', clickedStore.storeName);
            showStoreDetail(clickedStore);
        } else {
            console.log('✗ 모바일에서 선택된 상점 없음');
        }
    }

    isTouchMove = false;
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
