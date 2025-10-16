<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 변경 - 관문상가시장 관리자</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/assets/css/style.css" rel="stylesheet">
</head>
<body>
    <!-- 헤더 -->
    <div class="admin-header">
        <div class="container-fluid">
            <div class="d-flex justify-content-between align-items-center">
                <h2 class="mb-0">🔐 비밀번호 변경</h2>
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
    <div class="container-fluid p-4">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h4 class="mb-0">비밀번호 변경</h4>
                    </div>
                    <div class="card-body">
                        <form id="changePasswordForm">
                            <div class="mb-3">
                                <label for="oldPassword" class="form-label">현재 비밀번호 *</label>
                                <input type="password" class="form-control" id="oldPassword" name="oldPassword" required>
                                <div class="form-text">현재 사용 중인 비밀번호를 입력하세요.</div>
                            </div>
                            
                            <div class="mb-3">
                                <label for="newPassword" class="form-label">새 비밀번호 *</label>
                                <input type="password" class="form-control" id="newPassword" name="newPassword" required>
                                <div class="form-text">
                                    <small>
                                        • 최소 8자 이상<br>
                                        • 대문자, 소문자, 숫자, 특수문자 포함<br>
                                        • 최대 50자까지
                                    </small>
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label for="confirmPassword" class="form-label">새 비밀번호 확인 *</label>
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                                <div class="form-text">새 비밀번호를 다시 입력하세요.</div>
                            </div>
                            
                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary btn-lg">비밀번호 변경</button>
                                <button type="button" class="btn btn-secondary" onclick="cancelForm()">취소</button>
                            </div>
                        </form>
                    </div>
                </div>
                
                <!-- 비밀번호 강도 표시 -->
                <div class="card mt-3">
                    <div class="card-header">
                        <h6 class="mb-0">비밀번호 강도</h6>
                    </div>
                    <div class="card-body">
                        <div class="progress mb-2" style="height: 10px;">
                            <div class="progress-bar" id="passwordStrengthBar" role="progressbar" style="width: 0%"></div>
                        </div>
                        <div id="passwordStrengthText" class="small text-muted">비밀번호를 입력하세요.</div>
                    </div>
                </div>
                
                <!-- 보안 팁 -->
                <div class="card mt-3">
                    <div class="card-header">
                        <h6 class="mb-0">🔒 보안 팁</h6>
                    </div>
                    <div class="card-body">
                        <ul class="list-unstyled mb-0">
                            <li>• 정기적으로 비밀번호를 변경하세요</li>
                            <li>• 다른 사이트와 다른 비밀번호를 사용하세요</li>
                            <li>• 개인정보가 포함된 비밀번호는 피하세요</li>
                            <li>• 비밀번호는 안전한 곳에 보관하세요</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 알림 메시지 -->
    <div class="alert-overlay" id="alertOverlay"></div>
    <div class="alert-custom" id="alertMessage"></div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 비밀번호 강도 검사
        function checkPasswordStrength(password) {
            let score = 0;
            let feedback = [];
            
            if (password.length >= 8) score += 1;
            else feedback.push('최소 8자 이상');
            
            if (/[A-Z]/.test(password)) score += 1;
            else feedback.push('대문자 포함');
            
            if (/[a-z]/.test(password)) score += 1;
            else feedback.push('소문자 포함');
            
            if (/\d/.test(password)) score += 1;
            else feedback.push('숫자 포함');
            
            if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score += 1;
            else feedback.push('특수문자 포함');
            
            return { score, feedback };
        }

        // 비밀번호 강도 표시 업데이트
        function updatePasswordStrength(password) {
            const strengthBar = document.getElementById('passwordStrengthBar');
            const strengthText = document.getElementById('passwordStrengthText');
            
            if (!password) {
                strengthBar.style.width = '0%';
                strengthBar.className = 'progress-bar';
                strengthText.textContent = '비밀번호를 입력하세요.';
                return;
            }
            
            const { score, feedback } = checkPasswordStrength(password);
            const percentage = (score / 5) * 100;
            
            strengthBar.style.width = percentage + '%';
            
            if (score <= 2) {
                strengthBar.className = 'progress-bar bg-danger';
                strengthText.textContent = '약함 - ' + feedback.join(', ');
            } else if (score <= 3) {
                strengthBar.className = 'progress-bar bg-warning';
                strengthText.textContent = '보통 - ' + feedback.join(', ');
            } else if (score <= 4) {
                strengthBar.className = 'progress-bar bg-info';
                strengthText.textContent = '강함 - ' + feedback.join(', ');
            } else {
                strengthBar.className = 'progress-bar bg-success';
                strengthText.textContent = '매우 강함 - 완벽합니다!';
            }
        }

        // 새 비밀번호 입력 시 강도 검사
        document.getElementById('newPassword').addEventListener('input', function() {
            updatePasswordStrength(this.value);
        });

        // 비밀번호 확인 검증
        function validatePasswordMatch() {
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const confirmField = document.getElementById('confirmPassword');
            
            if (confirmPassword && newPassword !== confirmPassword) {
                confirmField.setCustomValidity('비밀번호가 일치하지 않습니다.');
                return false;
            } else {
                confirmField.setCustomValidity('');
                return true;
            }
        }

        // 비밀번호 확인 입력 시 검증
        document.getElementById('confirmPassword').addEventListener('input', validatePasswordMatch);
        document.getElementById('newPassword').addEventListener('input', validatePasswordMatch);

        // 폼 제출 처리
        document.getElementById('changePasswordForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            
            console.log('=== 비밀번호 변경 프로세스 시작 ===');
            console.log('타임스탬프:', new Date().toISOString());
            
            const oldPassword = document.getElementById('oldPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            console.log('입력값 존재 여부:', {
                oldPassword: !!oldPassword,
                newPassword: !!newPassword,
                confirmPassword: !!confirmPassword
            });
            
            // 유효성 검사
            if (!validatePasswordMatch()) {
                console.warn('검증 실패: 비밀번호 불일치');
                showAlert('비밀번호 확인이 일치하지 않습니다.', 'error');
                return;
            }
            
            const { score } = checkPasswordStrength(newPassword);
            console.log('비밀번호 강도 점수:', score);
            if (score < 3) {
                console.warn('검증 실패: 비밀번호 강도 부족');
                showAlert('더 강한 비밀번호를 사용해주세요.', 'error');
                return;
            }
            
            console.log('유효성 검사 통과');
            
            try {
                const formData = new FormData();
                formData.append('oldPassword', oldPassword);
                formData.append('newPassword', newPassword);
                
                console.log('FormData 생성 완료');
                console.log('API 호출 시작: POST /api/admin/change-password');
                
                const fetchStartTime = performance.now();
                
                const response = await fetch('/api/admin/change-password', {
                    method: 'POST',
                    body: formData,
                    credentials: 'same-origin'  // 쿠키/세션 포함
                });
                
                const fetchEndTime = performance.now();
                console.log('API 응답 시간:', (fetchEndTime - fetchStartTime).toFixed(2), 'ms');
                console.log('HTTP 상태:', response.status, response.statusText);
                console.log('응답 헤더:', {
                    contentType: response.headers.get('content-type'),
                    contentLength: response.headers.get('content-length')
                });
                
                const contentType = response.headers.get('content-type');
                
                let result;
                if (contentType && contentType.includes('application/json')) {
                    const responseText = await response.text();
                    console.log('응답 원본 텍스트:', responseText);
                    try {
                        result = JSON.parse(responseText);
                        console.log('파싱된 JSON:', result);
                    } catch (parseError) {
                        console.error('JSON 파싱 오류:', parseError);
                        showAlert('서버 응답을 처리할 수 없습니다.', 'error');
                        return;
                    }
                } else {
                    const text = await response.text();
                    console.error('예상치 못한 Content-Type:', contentType);
                    console.error('응답 내용:', text);
                    showAlert('서버 응답 형식이 올바르지 않습니다.', 'error');
                    return;
                }
                
                // 401 Unauthorized 처리
                if (response.status === 401) {
                    console.warn('인증 실패 - 로그인 페이지로 이동');
                    showAlert('로그인이 필요합니다. 다시 로그인 해주세요.', 'error');
                    setTimeout(() => {
                        location.href = '/admin/login';
                    }, 2000);
                    return;
                }
                
                // 성공/실패 처리
                console.log('result.success 값:', result.success, '(타입:', typeof result.success, ')');
                
                if (result.success === true || result.success === 'true') {
                    console.log('✅ 비밀번호 변경 성공');
                    showAlert('비밀번호가 성공적으로 변경되었습니다.', 'success');
                    resetForm();
                } else {
                    console.error('❌ 비밀번호 변경 실패:', result.message);
                    showAlert(result.message || '비밀번호 변경에 실패했습니다.', 'error');
                }
                
                console.log('=== 비밀번호 변경 프로세스 종료 ===');
                
            } catch (error) {
                console.error('=== 비밀번호 변경 중 예외 발생 ===');
                console.error('오류 타입:', error.name);
                console.error('오류 메시지:', error.message);
                console.error('스택 트레이스:', error.stack);
                showAlert('비밀번호 변경 중 오류가 발생했습니다: ' + error.message, 'error');
            }
        });

        // 폼 초기화
        function resetForm() {
            document.getElementById('changePasswordForm').reset();
            document.getElementById('passwordStrengthBar').style.width = '0%';
            document.getElementById('passwordStrengthBar').className = 'progress-bar';
            document.getElementById('passwordStrengthText').textContent = '비밀번호를 입력하세요.';
            
            // 커스텀 유효성 메시지 초기화
            document.getElementById('confirmPassword').setCustomValidity('');
        }

        // 취소 버튼 핸들러
        function cancelForm() {
            if (confirm('작성 중인 내용을 취소하시겠습니까?')) {
                resetForm();
                showAlert('입력 내용이 초기화되었습니다.', 'info');
            }
        }

        // 알림 메시지
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
            
            // 자동 숨김 (성공 메시지는 3초, 오류 메시지는 5초)
            const hideDelay = type === 'success' ? 3000 : 5000;
            
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

        // 로그아웃
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
    </script>
</body>
</html>
