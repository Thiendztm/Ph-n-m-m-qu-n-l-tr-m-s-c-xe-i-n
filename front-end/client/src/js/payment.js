let currentPlan = 'usage';   // usage | once | monthly
let hideEnergy = false;

const totalEl = document.getElementById('totalAmount');
const energyEl = document.querySelector('.detail-item:nth-child(3)');
const pricePerKwhEl = document.querySelector('.detail-item:nth-child(4)');
const detailItemPrice = document.querySelector('.detail-item-price');
const paymentDetails = document.querySelector('.payment-details.card');
const otpSection = document.getElementById('otpSection');
const otpInput = document.getElementById('otp');

// === 1. Thêm dòng "Gói sạc" vào đầu danh sách chi tiết ===
const planTitleEl = document.createElement('div');
planTitleEl.className = 'detail-item';
planTitleEl.innerHTML = `<span>Gói sạc:</span> <strong id="selectedPlan">Chưa chọn</strong>`;
paymentDetails.querySelector('.box__detail-items').prepend(planTitleEl);

// === 2. DOMContentLoaded - Khôi phục dữ liệu + điền thông tin trạm ===
document.addEventListener('DOMContentLoaded', () => {
    // --- Điền thông tin trạm sạc từ localStorage ---
    const saved = localStorage.getItem('bookingStation');
    if (!saved) {
        alert('Không có thông tin trạm sạc. Vui lòng đặt lại.');
        window.location.href = 'index.html';
        return;
    }

    let station;
    try {
        station = JSON.parse(saved);
    } catch (e) {
        alert('Dữ liệu trạm sạc bị lỗi!');
        window.location.href = 'index.html';
        return;
    }

    document.getElementById('stationName').textContent = station.name;
    document.getElementById('connectorType').textContent = station.connectorDisplay;
    document.getElementById('pricePerKWh').textContent = station.priceDisplay;
    document.getElementById('energyAmount').textContent = '0 kWh';

    // Lưu giá để tính toán sau
    window.currentStationPrice = station.price;

    // --- Khôi phục gói đã chọn trước đó ---
    const savedPlan = localStorage.getItem('selectedPlan');
    const savedAmount = localStorage.getItem('planAmount');
    const savedHide = localStorage.getItem('hideEnergy') === 'true';
    const savedCurrentPlan = localStorage.getItem('currentPlan') || 'usage';

    if (savedPlan) {
        document.getElementById('selectedPlan').textContent = savedPlan;
        totalEl.textContent = savedAmount || '0đ';
        hideEnergy = savedHide;
        currentPlan = savedCurrentPlan;

        if (hideEnergy) {
            energyEl.style.display = 'none';
            pricePerKwhEl.style.display = 'none';
            if (detailItemPrice) detailItemPrice.style.display = 'none';
        } else {
            energyEl.style.display = '';
            pricePerKwhEl.style.display = '';
            if (detailItemPrice) detailItemPrice.style.display = '';
        }
    }

    // Cập nhật tổng tiền lần đầu
    updateTotalAmount();
});

// === 3. Xử lý chọn gói sạc ===
document.querySelectorAll('.btn-option').forEach((btn, index) => {
    btn.addEventListener('click', () => {
        let planName = '';
        let planPrice = '';
        hideEnergy = false;
        currentPlan = 'usage';

        if (index === 0) {
            // Gói Linh Hoạt
            planName = 'Gói Linh Hoạt';
            planPrice = '0đ';
            hideEnergy = false;
            currentPlan = 'usage';
        } else if (index === 1) {
            // Gói Một Lần
            planName = 'Gói Một Lần';
            planPrice = document.querySelectorAll('.option-price')[0].textContent;
            hideEnergy = true;
            currentPlan = 'once';
        } else if (index === 2) {
            // Gói Tháng
            planName = 'Gói Tháng';
            planPrice = document.querySelectorAll('.option-price')[1].textContent;
            hideEnergy = true;
            currentPlan = 'monthly';
        }

        // Cập nhật giao diện
        document.getElementById('selectedPlan').textContent = planName;
        totalEl.textContent = planPrice;

        // Ẩn/hiện chi tiết năng lượng
        if (hideEnergy) {
            energyEl.style.display = 'none';
            pricePerKwhEl.style.display = 'none';
            if (detailItemPrice) detailItemPrice.style.display = 'none';
        } else {
            energyEl.style.display = '';
            pricePerKwhEl.style.display = '';
            if (detailItemPrice) detailItemPrice.style.display = '';
        }

        // Lưu vào localStorage
        localStorage.setItem('selectedPlan', planName);
        localStorage.setItem('planAmount', planPrice);
        localStorage.setItem('hideEnergy', hideEnergy);
        localStorage.setItem('currentPlan', currentPlan);

        // Cập nhật tổng tiền
        updateTotalAmount();
    });
});

// === 4. Hàm cập nhật tổng tiền ===
function updateTotalAmount() {
    let amount = 0;

    if (currentPlan === 'once') {
        amount = 60000;
    } else if (currentPlan === 'monthly') {
        amount = 1000000;
    } else if (currentPlan === 'usage') {
        // Có thể mở rộng: nhập kWh → tính tiền
        amount = 51000; // tạm tính ví dụ
    }

    if (!hideEnergy || currentPlan === 'usage') {
        totalEl.textContent = amount.toLocaleString('vi-VN') + 'đ';
    }
}

// === 5. Xử lý thanh toán ===
document.getElementById('paymentForm')?.addEventListener('submit', (e) => {
    e.preventDefault();

    const method = document.querySelector('input[name="method"]:checked').value;

    // Cập nhật lại tổng tiền trước khi thanh toán
    updateTotalAmount();
    const total = parseInt(totalEl.textContent.replace(/\D/g, '')) || 0;

    if (method === 'ev') {
        const balance = parseInt(document.getElementById('walletBalance').textContent.replace(/\D/g, '')) || 0;

        if (balance >= total) {
            alert(`Thanh toán thành công bằng Ví EV!\nĐã trừ: ${total.toLocaleString()}đ\nSố dư còn lại: ${(balance - total).toLocaleString()}đ`);
            localStorage.setItem('bookingStatus', 'success');
            window.location.href = 'index.html';
        } else {
            alert('Số dư ví không đủ!');
        }
    } else {
        // Thanh toán ngân hàng/thẻ → hiện OTP
        otpSection.style.display = 'block';
        otpInput.value = '';
        otpInput.focus();

        // Gỡ listener cũ để tránh bị ghi đè
        otpInput.oninput = null;
        otpInput.addEventListener('input', function handler() {
            if (this.value.length === 6) {
                alert(`Thanh toán thành công bằng ${method === 'bank' ? 'Ngân hàng' : 'Thẻ tín dụng'}!`);
                otpSection.style.display = 'none';
                this.value = '';
                otpInput.removeEventListener('input', handler);
                localStorage.setItem('bookingStatus', 'success');
                window.location.href = 'index.html';
            }
        });
    }
});

// === 6. Ẩn OTP khi đổi phương thức thanh toán ===
document.querySelectorAll('input[name="method"]').forEach((radio) => {
    radio.addEventListener('change', () => {
        otpSection.style.display = 'none';
    });
});

// === 7. Hủy thanh toán ===
document.querySelector('.btn-report')?.addEventListener('click', () => {
    localStorage.setItem('bookingStatus', 'cancel');
    window.location.href = 'index.html';
});