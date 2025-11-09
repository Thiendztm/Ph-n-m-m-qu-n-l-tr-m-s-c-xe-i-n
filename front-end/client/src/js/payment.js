// --- Lấy các phần tử cần thao tác ---
const totalEl = document.getElementById('totalAmount');
const energyEl = document.querySelector('.detail-item:nth-child(3)');
const pricePerKwhEl = document.querySelector('.detail-item:nth-child(4)');
const detailItemPrice = document.querySelector('.detail-item-price');
const paymentDetails = document.querySelector('.payment-details.card');

// Thêm dòng hiển thị "Gói sạc"
const planTitleEl = document.createElement('div');
planTitleEl.className = 'detail-item';
planTitleEl.innerHTML = `<span>Gói sạc:</span> <strong id="selectedPlan">Chưa chọn</strong>`;
paymentDetails.querySelector('.box__detail-items').prepend(planTitleEl);

// Biến trạng thái
let currentPlan = 'usage'; // Mặc định gói linh hoạt
let hideEnergy = false;

// --- Xử lý chọn gói ---
document.querySelectorAll('.btn-option').forEach((btn, index) => {
  btn.addEventListener('click', () => {
    let planName = '';
    let planPrice = '';

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

    // Cập nhật hiển thị
    document.getElementById('selectedPlan').textContent = planName;
    totalEl.textContent = planPrice;

    // Ẩn hoặc hiện các phần chi tiết
    if (hideEnergy) {
      energyEl.style.display = 'none';
      pricePerKwhEl.style.display = 'none';
      if (detailItemPrice) detailItemPrice.style.display = 'none';
    } else {
      energyEl.style.display = '';
      pricePerKwhEl.style.display = '';
      if (detailItemPrice) detailItemPrice.style.display = '';
    }

    // Lưu vào localStorage để ghi nhớ
    localStorage.setItem('selectedPlan', planName);
    localStorage.setItem('planAmount', planPrice);
    localStorage.setItem('hideEnergy', hideEnergy);
    localStorage.setItem('currentPlan', currentPlan);
  });
});

// --- Khôi phục lựa chọn khi tải lại trang ---
window.addEventListener('DOMContentLoaded', () => {
  const savedPlan = localStorage.getItem('selectedPlan');
  const savedAmount = localStorage.getItem('planAmount');
  const savedHide = localStorage.getItem('hideEnergy') === 'true';

  if (savedPlan) {
    document.getElementById('selectedPlan').textContent = savedPlan;
    totalEl.textContent = savedAmount || '0đ';
    hideEnergy = savedHide;

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
});

// --- Xử lý thanh toán ---
document.getElementById('paymentForm').addEventListener('submit', (e) => {
  e.preventDefault();

  const method = document.querySelector('input[name="method"]:checked').value;
  const otpSection = document.getElementById('otpSection');

  // Cập nhật tổng tiền theo gói
  let planAmount =
    currentPlan === 'once'
      ? 60000
      : currentPlan === 'monthly'
      ? 1000000
      : 51000;

  if (!hideEnergy) {
    totalEl.textContent = planAmount.toLocaleString('vi-VN') + 'đ';
  }

  if (method === 'ev') {
    // Thanh toán bằng ví EV
    const balance = parseInt(
      document.getElementById('walletBalance').textContent.replace(/\D/g, '')
    );
    const total = parseInt(totalEl.textContent.replace(/\D/g, ''));

    if (balance >= total) {
      alert(
        `✅ Thanh toán thành công bằng Ví EV!\nĐã trừ: ${total.toLocaleString()}đ\nSố dư còn lại: ${(balance - total).toLocaleString()}đ`
      );
      localStorage.setItem('bookingStatus', 'success');
      window.location.href = 'index.html';
    } else {
      alert('⚠️ Số dư ví không đủ!');
    }
  } else {
    // Hiển thị OTP nếu là ngân hàng / thẻ
    otpSection.style.display = 'block';
    document.getElementById('otp').focus();

    document.getElementById('otp').addEventListener('input', function () {
      if (this.value.length === 6) {
        alert(
          '✅ Thanh toán thành công bằng ' +
            (method === 'bank' ? 'Ngân hàng' : 'Thẻ tín dụng') +
            '!'
        );
        otpSection.style.display = 'none';
        this.value = '';
        localStorage.setItem('bookingStatus', 'success');
        window.location.href = 'index.html';
      }
    });
  }
});

// Ẩn OTP khi đổi phương thức
document.querySelectorAll('input[name="method"]').forEach((radio) => {
  radio.addEventListener('change', () => {
    document.getElementById('otpSection').style.display = 'none';
  });
});

// --- Hủy thanh toán ---
document.querySelector('.btn-report').addEventListener('click', () => {
  localStorage.setItem('bookingStatus', 'cancel');
  window.location.href = 'index.html';
});
