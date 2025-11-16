// Staff Login Script
const API_BASE_URL = window.API_BASE_URL || 'http://localhost:8080/api';

const loginForm = document.getElementById('loginForm');
const loginBtn = document.getElementById('loginBtn');
const errorMessage = document.getElementById('errorMessage');
const errorText = document.getElementById('errorText');

// Check if already logged in
checkExistingSession();

function checkExistingSession() {
    const token = localStorage.getItem('accessToken');
    const role = localStorage.getItem('userRole');
    
    if (token && (role === 'CS_STAFF' || role === 'ADMIN')) {
        // Already logged in, redirect to dashboard
        window.location.href = 'index.html';
    }
}

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const remember = document.getElementById('remember').checked;

    // Show loading
    loginBtn.disabled = true;
    loginBtn.classList.add('loading');
    loginBtn.textContent = 'Đang đăng nhập';
    hideError();

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Đăng nhập thất bại');
        }

        // Check if user has CS_STAFF or ADMIN role
        if (data.role !== 'CS_STAFF' && data.role !== 'ADMIN') {
            throw new Error('Bạn không có quyền truy cập. Vui lòng sử dụng tài khoản nhân viên.');
        }

        // Save user session
        saveUserSession(data, remember);

        // Success notification
        showSuccessNotification();

        // Redirect to dashboard
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 800);

    } catch (error) {
        console.error('Login error:', error);
        showError(error.message || 'Email hoặc mật khẩu không đúng');
        
        // Reset button
        loginBtn.disabled = false;
        loginBtn.classList.remove('loading');
        loginBtn.textContent = 'Đăng nhập';
    }
});

function saveUserSession(data, remember) {
    // Save to localStorage for persistence
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('userRole', data.role);
    localStorage.setItem('userEmail', data.email || '');
    localStorage.setItem('userId', data.userId || '');
    localStorage.setItem('userName', data.name || '');
    localStorage.setItem('loginTime', new Date().toISOString());
    
    if (data.refreshToken) {
        localStorage.setItem('refreshToken', data.refreshToken);
    }

    // If remember me is checked, also save to sessionStorage
    if (remember) {
        sessionStorage.setItem('rememberMe', 'true');
    }
}

function showError(message) {
    errorText.textContent = message;
    errorMessage.classList.add('show');
}

function hideError() {
    errorMessage.classList.remove('show');
}

function showSuccessNotification() {
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #10b981;
        color: white;
        padding: 16px 20px;
        border-radius: 12px;
        font-weight: 600;
        box-shadow: 0 8px 24px rgba(16, 185, 129, 0.3);
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
    `;
    notification.innerHTML = '<i class="fas fa-check-circle" style="margin-right: 8px;"></i>Đăng nhập thành công!';
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 2000);
}

// Add CSS for animation
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
`;
document.head.appendChild(style);
