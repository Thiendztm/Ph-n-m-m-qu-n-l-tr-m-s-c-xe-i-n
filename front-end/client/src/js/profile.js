// Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Elements
const userInfoSection = document.getElementById('userInfoSection');
const editBtn = document.getElementById('editBtn');
const infoView = document.getElementById('infoView');
const infoEdit = document.getElementById('infoEdit');
const cancelEdit = document.getElementById('cancelEdit');
const editProfileForm = document.getElementById('editProfileForm');
const avatarImg = document.getElementById('avatarImg');
const previewImg = document.getElementById('previewImg');
const editAvatar = document.getElementById('editAvatar');
const displayName = document.getElementById('displayName');
const displayEmail = document.getElementById('displayEmail');
const displayPhone = document.getElementById('displayPhone');
const displayVehicle = document.getElementById('displayVehicle');
const profileWalletBalance = document.getElementById('profileWalletBalance');
const editName = document.getElementById('editName');
const editEmail = document.getElementById('editEmail');
const editPhone = document.getElementById('editPhone');
const editVehicle = document.getElementById('editVehicle');

// === Wallet Balance Management ===
async function fetchAndUpdateWalletBalance() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return 0;

    try {
        const response = await fetch(`${API_BASE_URL}/profile/wallet`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const data = await response.json();
            const balance = data.balance || 0;
            localStorage.setItem('walletBalance', balance.toString());
            return balance;
        }
    } catch (error) {
        console.error('Error fetching wallet balance:', error);
    }
    
    // Fallback to localStorage
    return parseFloat(localStorage.getItem('walletBalance') || '0');
}

function updateProfileWalletDisplay(balance) {
    if (profileWalletBalance) {
        profileWalletBalance.textContent = balance.toLocaleString('vi-VN') + 'đ';
        
        // Add color coding based on balance
        if (balance >= 1000000) {
            profileWalletBalance.style.color = '#28a745'; // Green for good balance
        } else if (balance >= 500000) {
            profileWalletBalance.style.color = '#fd7e14'; // Orange for medium balance
        } else {
            profileWalletBalance.style.color = '#dc3545'; // Red for low balance
        }
    }
}

async function displayProfile() {
    try {
        const token = localStorage.getItem('jwt_token');
        if (!token) {
            console.error('No token found');
            alert('Vui lòng đăng nhập để xem hồ sơ');
            window.location.href = 'login.html';
            return;
        }

        const response = await fetch(`${API_BASE_URL}/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Profile data received:', data);

            // Update form fields
            if (data.hoTen) document.getElementById('hoTen').value = data.hoTen;
            if (data.email) document.getElementById('email').value = data.email;
            if (data.sdt) document.getElementById('sdt').value = data.sdt;
            if (data.diaChi) document.getElementById('diaChi').value = data.diaChi;

            // Fetch and update wallet balance separately
            const balance = await fetchAndUpdateWalletBalance();
            updateProfileWalletDisplay(balance);

            // Update vehicle data if exists
            if (data.xe && data.xe.length > 0) {
                const xe = data.xe[0]; // Get first vehicle
                if (xe.bienSo) document.getElementById('vehicle-number').value = xe.bienSo;
                if (xe.loaiXe) document.getElementById('vehicle-type').value = xe.loaiXe;
                if (xe.dongXe) document.getElementById('vehicle-model').value = xe.dongXe;
                if (xe.phienBan) document.getElementById('vehicle-version').value = xe.phienBan;
                if (xe.namSanXuat) document.getElementById('vehicle-year').value = xe.namSanXuat;
            }

        } else if (response.status === 401) {
            alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
            localStorage.removeItem('jwt_token');
            window.location.href = 'login.html';
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    } catch (error) {
        console.error('Error displaying profile:', error);
        alert('Có lỗi xảy ra khi tải thông tin hồ sơ');
    }
}

// Load profile data on page load
document.addEventListener('DOMContentLoaded', () => {
    displayProfile();
});

// Toggle edit mode
editBtn.addEventListener('click', () => {
    infoView.style.display = 'none';
    infoEdit.style.display = 'block';
    editName.value = displayName.textContent;
    editEmail.value = displayEmail.textContent;
    editPhone.value = displayPhone.textContent;
    editVehicle.value = displayVehicle.textContent;
});

cancelEdit.addEventListener('click', () => {
    infoEdit.style.display = 'none';
    infoView.style.display = 'block';
});

// Preview and upload image from device
editAvatar.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            previewImg.src = e.target.result;
            avatarImg.src = e.target.result; // Cập nhật ảnh ngay khi chọn
        };
        reader.readAsDataURL(file); // Truy cập bộ nhớ để tải ảnh
    }
});

// Save changes
editProfileForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    displayName.textContent = editName.value;
    displayEmail.textContent = editEmail.value;
    displayPhone.textContent = editPhone.value;
    displayVehicle.textContent = editVehicle.value;
    
    infoEdit.style.display = 'none';
    infoView.style.display = 'block';
    alert('Hồ sơ đã được cập nhật thành công!');
    // TODO: Gọi API để lưu dữ liệu
});