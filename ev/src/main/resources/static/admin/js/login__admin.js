// Import API client
import { api, setTokens } from './api-client.js';

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("adminLoginForm");
    const emailInput = document.getElementById("adminEmail");
    const passwordInput = document.getElementById("adminPassword");
    const submitBtn = document.querySelector(".login-button");

    // Hiển thị thông báo lỗi
    function showError(message) {
        // Xóa thông báo cũ nếu có
        const existingError = document.querySelector(".error-message");
        if (existingError) existingError.remove();

        const errorDiv = document.createElement("div");
        errorDiv.className = "error-message";
        errorDiv.style.cssText = `
            background: #fee;
            color: #c53030;
            padding: 12px 16px;
            border-radius: 8px;
            margin-top: 16px;
            font-size: 14px;
            border: 1px solid #feb2b2;
            text-align: center;
        `;
        errorDiv.textContent = message;
        form.appendChild(errorDiv);

        // Tự động ẩn sau 5 giây
        setTimeout(() => {
            if (errorDiv && errorDiv.parentElement) {
                errorDiv.remove();
            }
        }, 5000);
    }

    // Xóa lỗi khi người dùng nhập lại
    function clearError() {
        const error = document.querySelector(".error-message");
        if (error) error.remove();
    }

    // Hiệu ứng loading cho nút
    function setLoading(loading) {
        if (loading) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...';
        } else {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Đăng nhập';
        }
    }

    // Xử lý đăng nhập
    form.addEventListener("submit", function (e) {
        e.preventDefault();
        clearError();

        const email = emailInput.value.trim().toLowerCase();
        const password = passwordInput.value;

        // Kiểm tra rỗng
        if (!email || !password) {
            showError("Vui lòng nhập đầy đủ email và mật khẩu!");
            return;
        }

        // Kiểm tra định dạng email đơn giản
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showError("Email không hợp lệ!");
            return;
        }

        setLoading(true);

        // Call login API
        api.post('/auth/login', { email, password })
            .then(response => {
                // Save tokens and user info
                setTokens(response.accessToken, response.refreshToken);
                localStorage.setItem("adminLoggedIn", "true");
                localStorage.setItem("adminEmail", email);
                localStorage.setItem("userName", response.fullName || response.email);
                localStorage.setItem("userRole", response.role);
                localStorage.setItem("userId", response.id);

                // Check if user is admin or staff
                if (response.role === 'ADMIN' || response.role === 'CS_STAFF') {
                    window.location.href = "./index.html";
                } else {
                    // User is EV_DRIVER, redirect to client page
                    showError("Tài khoản này không có quyền truy cập trang admin!");
                    localStorage.clear();
                    setLoading(false);
                    setTimeout(() => {
                        window.location.href = "../role-selection.html";
                    }, 2000);
                }
            })
            .catch(error => {
                console.error('Login failed:', error);
                showError(error.message || "Email hoặc mật khẩu không đúng!");
                setLoading(false);
            });
    });

    // Xóa lỗi khi người dùng nhập
    emailInput.addEventListener("input", clearError);
    passwordInput.addEventListener("input", clearError);

    // Cho phép nhấn Enter ở bất kỳ input nào cũng submit
    [emailInput, passwordInput].forEach(input => {
        input.addEventListener("keypress", function (e) {
            if (e.key === "Enter") {
                form.dispatchEvent(new Event("submit"));
            }
        });
    });

    // Tự động focus vào ô email khi load trang
    emailInput.focus();
});