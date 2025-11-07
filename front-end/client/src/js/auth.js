
const inpUserName = document.querySelector("#username");
const inpEmail = document.querySelector("#email");
const inpPwd = document.querySelector("#password");
const inpConfirmPwd = document.querySelector("#confirm-password");
const inpVehicleType = document.querySelector("#vehicle-type");
const inpVehicleInfo = document.querySelector("#vehicle-info");
const inpOtp = document.querySelector("#otp");
const regMessage = document.querySelector("#regMessage");
const registerForm = document.querySelector("#register-form");
const sendOtpBtn = document.querySelector("#sendOtpBtn");
const registerBtn = document.querySelector("#registerBtn");
const otpSection = document.querySelector("#otpSection");

const togglePassword = document.getElementById("togglePassword");
const passwordInput = document.getElementById("password");

// ---------- REGISTER ----------
if (registerForm && inpUserName && inpConfirmPwd) {
  let otpSent = false;
  let correctOtp = null;

  // Gửi OTP
  sendOtpBtn.addEventListener('click', () => {
    const email = inpEmail.value.trim();
    if (!email) {
      regMessage.innerText = "Vui lòng nhập email để gửi OTP.";
      regMessage.style.color = "red";
      return;
    }
    // Mẫu: Tạo OTP ngẫu nhiên
    correctOtp = Math.floor(100000 + Math.random() * 900000).toString();
    regMessage.innerText = `OTP đã được gửi đến ${email}. Mã OTP: ${correctOtp} (Mẫu)`;
    regMessage.style.color = "green";
    otpSection.style.display = 'block';
    otpSent = true;
    registerBtn.disabled = false; // Kích hoạt nút Đăng ký sau khi gửi OTP
  });

  registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    let username = inpUserName.value.trim();
    let email = inpEmail.value.trim();
    let password = inpPwd.value.trim();
    let configPassword = inpConfirmPwd.value.trim();
    let vehicleType = inpVehicleType.value;
    let vehicleInfo = inpVehicleInfo.value.trim();
    let otp = inpOtp.value.trim();

    let lowerCaseLetter = /[a-z]/g;
    let upperCaseLetter = /[A-Z]/g;
    let numbers = /[0-9]/g;

    if (!username || !email || !password || !configPassword || !vehicleType || !vehicleInfo) {
      regMessage.innerText = "Vui lòng điền đầy đủ thông tin, bao gồm loại xe và thông tin xe.";
      regMessage.style.color = "red";
      return;
    }
    if (!otpSent) {
      regMessage.innerText = "Vui lòng gửi OTP trước khi đăng ký.";
      regMessage.style.color = "red";
      return;
    }
    if (otp !== correctOtp) {
      regMessage.innerText = "Mã OTP không đúng!";
      regMessage.style.color = "red";
      return;
    }
    if (password.length < 8) {
      regMessage.innerText = "Mật khẩu phải có ít nhất 8 ký tự";
      regMessage.style.color = "red";
      return;
    }
    if (!lowerCaseLetter.test(password)) {
      regMessage.innerText = "Mật khẩu chưa có chữ thường";
      regMessage.style.color = "red";
      return;
    }
    if (!upperCaseLetter.test(password)) {
      regMessage.innerText = "Mật khẩu chưa có chữ in hoa";
      regMessage.style.color = "red";
      return;
    }
    if (!numbers.test(password)) {
      regMessage.innerText = "Mật khẩu chưa có số 0-9";
      regMessage.style.color = "red";
      return;
    }
    if (password !== configPassword) {
      regMessage.innerText = "Mật khẩu không khớp!";
      regMessage.style.color = "red";
      return;
    }

    // Xử lý đăng ký thành công (mẫu)
    regMessage.innerText = "Đăng ký thành công! Loại xe: " + (vehicleType === "xe-may" ? "Xe máy điện" : "Xe hơi điện") + ", Thông tin: " + vehicleInfo;
    regMessage.style.color = "green";
    registerForm.reset();
    otpSection.style.display = 'none';
    otpSent = false;
    // TODO: Gọi API đăng ký với dữ liệu (username, email, password, vehicleType, vehicleInfo)
  });
}

// ---------- LOGIN ----------
const loginForm = document.getElementById("login-form");

if (loginForm) {
  loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    let email = inpEmail.value.trim();
    let password = inpPwd.value.trim();

    if (!email || !password) {
      regMessage.innerText = "Vui lòng điền đầy đủ thông tin.";
      regMessage.style.color = "red";
      return;
    }

    // Xử lý đăng nhập thành công (mẫu)
    regMessage.innerText = "Đăng nhập thành công!";
    regMessage.style.color = "green";
    // TODO: Gọi API đăng nhập
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