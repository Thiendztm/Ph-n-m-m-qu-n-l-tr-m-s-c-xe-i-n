document.getElementById('paymentForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const method = document.querySelector('input[name="method"]:checked').value;
    const otpSection = document.getElementById('otpSection');
    const selectedPlan = document.querySelector('input[name="plan"]:checked')?.value || 'usage';

    // Cập nhật tổng tiền dựa trên gói (demo)
    const totalEl = document.getElementById('totalAmount');
    const planAmount = selectedPlan === 'once' ? 60000 : (selectedPlan === 'monthly' ? 1000000 : 51000);
    if (totalEl) totalEl.textContent = planAmount.toLocaleString('vi-VN') + 'đ';
    
    if (method === 'ev') {
        // Thanh toán bằng ví EV
        const balance = parseInt(document.getElementById('walletBalance').textContent.replace(/\D/g, ''));
        const total = parseInt(document.getElementById('totalAmount').textContent.replace(/\D/g, ''));
        
        if (balance >= total) {
            alert(`Thanh toán thành công bằng Ví EV!\nĐã trừ: ${total.toLocaleString()}đ\nSố dư còn lại: ${(balance - total).toLocaleString()}đ`);
            localStorage.setItem('bookingStatus','success');
            window.location.href = 'index.html';
        } else {
            alert('Số dư ví không đủ!');
        }
    } else {
        // Hiển thị OTP cho ngân hàng/thẻ
        otpSection.style.display = 'block';
        document.getElementById('otp').focus();
        
        // Xử lý OTP (mẫu)
        document.getElementById('otp').addEventListener('input', function() {
            if (this.value.length === 6) {
                alert('Thanh toán thành công bằng ' + (method === 'bank' ? 'Ngân hàng' : 'Thẻ tín dụng') + '!');
                otpSection.style.display = 'none';
                this.value = '';
                localStorage.setItem('bookingStatus','success');
                window.location.href = 'index.html';
            }
        });
    }
});

// Tự động ẩn OTP khi đổi phương thức
document.querySelectorAll('input[name="method"]').forEach(radio => {
    radio.addEventListener('change', () => {
        document.getElementById('otpSection').style.display = 'none';
    });
});

// Hủy thanh toán
const btnCancel = document.getElementById('btnCancelPay');
if (btnCancel) {
    btnCancel.addEventListener('click', () => {
        localStorage.setItem('bookingStatus','cancel');
        window.location.href = 'index.html';
    });
}