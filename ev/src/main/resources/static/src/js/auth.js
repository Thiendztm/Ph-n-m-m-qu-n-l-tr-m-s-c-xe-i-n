
// ========== CONFIG ==========
const API_BASE_URL = 'http://localhost:8080/api';

// ========== DOM ELEMENTS ==========
const inpUserName = document.querySelector("#username");
const inpEmail = document.querySelector("#email");
const inpPwd = document.querySelector("#password");
const inpConfirmPwd = document.querySelector("#confirm-password");
const inpPhoneNumber = document.querySelector("#phone-number");
const inpVehicleInfo = document.querySelector("#vehicle-info");
const regMessage = document.querySelector("#regMessage");
const registerForm = document.querySelector("#register-form");

const togglePassword = document.getElementById("togglePassword");
const passwordInput = document.getElementById("password");

// ========== UTILITY FUNCTIONS ==========
function showMessage(message, isSuccess = true) {
  regMessage.innerText = message;
  regMessage.style.color = isSuccess ? "green" : "red";
}

function saveAuthData(authResponse) {
  localStorage.setItem('jwt_token', authResponse.accessToken);
  localStorage.setItem('refreshToken', authResponse.refreshToken);
  localStorage.setItem('userId', authResponse.userId);
  localStorage.setItem('userEmail', authResponse.email);
  localStorage.setItem('userRole', authResponse.role);
  localStorage.setItem('userName', `${authResponse.firstName} ${authResponse.lastName}`);
}

function clearAuthData() {
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('userId');
  localStorage.removeItem('userEmail');
  localStorage.removeItem('userRole');
  localStorage.removeItem('userName');
}

function isLoggedIn() {
  return localStorage.getItem('jwt_token') !== null;
}

// ========== REGISTER ==========
if (registerForm && inpUserName && inpConfirmPwd) {
  registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    // Get form values
    const fullName = inpUserName.value.trim();
    const email = inpEmail.value.trim();
    const password = inpPwd.value.trim();
    const confirmPassword = inpConfirmPwd.value.trim();
    const phoneNumber = inpPhoneNumber ? inpPhoneNumber.value.trim() : '';
    const vehicleInfo = inpVehicleInfo ? inpVehicleInfo.value.trim() : '';

    // Split full name into first and last name
    const nameParts = fullName.split(' ').filter(part => part.length > 0);
    const firstName = nameParts[0] || '';
    const lastName = nameParts.slice(1).join(' ') || '';

    // Validation
    const lowerCaseLetter = /[a-z]/g;
    const upperCaseLetter = /[A-Z]/g;
    const numbers = /[0-9]/g;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phoneRegex = /^(\+84|0)[0-9]{9,10}$/;

    if (!fullName || !email || !password || !confirmPassword || !phoneNumber) {
      showMessage("Vui lòng điền đầy đủ thông tin bắt buộc.", false);
      return;
    }

    // Check if full name has at least 2 words (first name + last name)
    if (nameParts.length < 2) {
      showMessage("Vui lòng nhập họ và tên đầy đủ (VD: Nguyen Van A)", false);
      return;
    }

    if (!phoneRegex.test(phoneNumber)) {
      showMessage("Số điện thoại không hợp lệ. VD: 0912345678 hoặc +84912345678", false);
      return;
    }

    if (!emailRegex.test(email)) {
      showMessage("Email không hợp lệ.", false);
      return;
    }

    if (password.length < 8) {
      showMessage("Mật khẩu phải có ít nhất 8 ký tự.", false);
      return;
    }

    if (!lowerCaseLetter.test(password)) {
      showMessage("Mật khẩu phải chứa ít nhất 1 chữ thường.", false);
      return;
    }

    if (!upperCaseLetter.test(password)) {
      showMessage("Mật khẩu phải chứa ít nhất 1 chữ in hoa.", false);
      return;
    }

    if (!numbers.test(password)) {
      showMessage("Mật khẩu phải chứa ít nhất 1 chữ số.", false);
      return;
    }

    if (password !== confirmPassword) {
      showMessage("Mật khẩu không khớp!", false);
      return;
    }

    // Parse vehicle info if provided
    let vehicleModel = '';
    let vehiclePlate = '';
    let connectorType = 'Type 2'; // Default
    
    if (vehicleInfo) {
      // Try to parse vehicle info in format: "Model - PlateNumber"
      // Or just treat it as plate number if no dash
      if (vehicleInfo.includes(' - ')) {
        const parts = vehicleInfo.split(' - ');
        vehicleModel = parts[0].trim();
        vehiclePlate = parts[1].trim();
      } else {
        // Assume it's just the plate number
        vehiclePlate = vehicleInfo.trim();
        vehicleModel = 'Unknown';
      }
    }

    // Call API
    try {
      showMessage("Đang đăng ký...", true);

      const requestData = {
        email: email,
        password: password,
        firstName: firstName,
        lastName: lastName,
        phoneNumber: phoneNumber
      };

      // Add vehicle info if provided
      if (vehiclePlate) {
        requestData.vehicleModel = vehicleModel;
        requestData.vehiclePlate = vehiclePlate;
        requestData.connectorType = connectorType;
      }

      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
      });

      const data = await response.json();

      if (response.ok) {
        // Save auth data
        saveAuthData(data);
        
        showMessage("Đăng ký thành công! Đang chuyển hướng...", true);
        
        // Redirect to login page after 1.5 seconds
        setTimeout(() => {
          window.location.href = 'login.html';
        }, 1500);
      } else {
        // Handle error from backend
        showMessage(data.message || "Đăng ký thất bại. Vui lòng thử lại.", false);
      }
    } catch (error) {
      console.error('Register error:', error);
      showMessage("Lỗi kết nối đến server. Vui lòng kiểm tra backend đang chạy.", false);
    }
  });
}

// ========== LOGIN ==========
const loginForm = document.getElementById("login-form");

if (loginForm && !inpUserName) { // Login form doesn't have username field
  loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const email = inpEmail.value.trim();
    const password = inpPwd.value.trim();

    // Validation
    if (!email || !password) {
      showMessage("Vui lòng điền đầy đủ thông tin.", false);
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      showMessage("Email không hợp lệ.", false);
      return;
    }

    // Call API
    try {
      showMessage("Đang đăng nhập...", true);

      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          password: password
        })
      });

      // Check if response is JSON
      const contentType = response.headers.get("content-type");
      if (!contentType || !contentType.includes("application/json")) {
        throw new Error("Server trả về lỗi không hợp lệ. Vui lòng kiểm tra backend.");
      }

      const data = await response.json();

      if (response.ok) {
        // Save auth data
        saveAuthData(data);
        
        showMessage("Đăng nhập thành công! Đang chuyển hướng...", true);
        
        // Redirect based on role
        setTimeout(() => {
          if (data.role === 'ADMIN') {
            window.location.href = 'analytics.html'; // Admin dashboard
          } else if (data.role === 'CS_STAFF') {
            window.location.href = 'analytics.html'; // Staff dashboard
          } else {
            window.location.href = 'index.html'; // EV Driver home
          }
        }, 1500);
      } else {
        // Handle error from backend
        const errorMessage = data.message || data.error || "Email hoặc mật khẩu không đúng.";
        showMessage(errorMessage, false);
      }
    } catch (error) {
      console.error('Login error:', error);
      showMessage("Lỗi kết nối. Email/mật khẩu không đúng hoặc tài khoản chưa tồn tại.", false);
    }
  });
}

// ---------- Dùng để xem hiển thị mật khẩu ----------
if (togglePassword && passwordInput) {
  togglePassword.addEventListener("click", function () {
    const isPassword = passwordInput.type === "password";
    passwordInput.type = isPassword ? "text" : "password";
    this.classList.toggle("fa-eye");
    this.classList.toggle("fa-eye-slash");
  });
}