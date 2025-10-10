<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>관리자 로그인 - 관문상가시장</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/static/css/style.css" rel="stylesheet">
</head>
<body>
    <div class="login-container">
        <div class="login-box">
            <h2 class="login-title">🔐 관리자 로그인</h2>
            
            <!-- 오류 메시지 표시 영역 -->
            <div id="alertContainer"></div>
            
            <form class="login-form" action="/api/admin/login" method="post" id="loginForm">
                <input type="text" 
                       class="login-input" 
                       id="username" 
                       name="username" 
                       placeholder="사용자명" 
                       required>
                
                <input type="password" 
                       class="login-input" 
                       id="password" 
                       name="password" 
                       placeholder="비밀번호" 
                       required>
                
                <button type="submit" class="login-btn" id="loginBtn">
                    <span id="loginBtnText">로그인</span>
                    <span id="loginSpinner" style="display: none;">로그인 중...</span>
                </button>
            </form>
            
            <div class="text-center mt-3">
                <small class="text-muted">
                </small>
            </div>
        </div>
    </div>

    <!-- 알림 메시지 -->
    <div class="alert-overlay" id="alertOverlay"></div>
    <div class="alert-custom" id="alertMessage"></div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 페이지 로드 시 URL 파라미터 확인하여 메시지 표시
        document.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            
            if (urlParams.get('error') === 'true') {
                showAlert('아이디 또는 비밀번호가 올바르지 않습니다.', 'error');
            } else if (urlParams.get('logout') === 'true') {
                showAlert('성공적으로 로그아웃되었습니다.', 'success');
            } else if (urlParams.get('expired') === 'true') {
                showAlert('세션이 만료되었습니다. 다시 로그인해주세요.', 'info');
            } else if (urlParams.get('accessDenied') === 'true') {
                showAlert('접근 권한이 없습니다.', 'error');
            }
            
            // 폼 제출 시 로딩 상태 표시
            document.getElementById('loginForm').addEventListener('submit', function() {
                const loginBtn = document.getElementById('loginBtn');
                const loginBtnText = document.getElementById('loginBtnText');
                const loginSpinner = document.getElementById('loginSpinner');
                
                loginBtn.disabled = true;
                loginBtnText.style.display = 'none';
                loginSpinner.style.display = 'inline';
            });
        });
        
        // 중앙 알림 메시지 표시
        function showAlert(message, type) {
            const alert = document.getElementById('alertMessage');
            const overlay = document.getElementById('alertOverlay');
            if (!alert || !overlay) return;
            
            // 오버레이 표시
            overlay.style.display = 'block';
            
            // 알림 메시지 설정
            alert.className = `alert-custom alert-${type}`;
            alert.textContent = message;
            alert.style.display = 'block';
            
            // 자동 숨김 (성공 메시지는 3초, 오류 메시지는 5초, 정보 메시지는 4초)
            let hideDelay;
            if (type === 'success') hideDelay = 3000;
            else if (type === 'info') hideDelay = 4000;
            else hideDelay = 5000;
            
            setTimeout(() => {
                alert.style.display = 'none';
                overlay.style.display = 'none';
            }, hideDelay);
            
            // 오버레이 클릭 시 닫기
            overlay.onclick = () => {
                alert.style.display = 'none';
                overlay.style.display = 'none';
            };
        }
    </script>
</body>
</html>
