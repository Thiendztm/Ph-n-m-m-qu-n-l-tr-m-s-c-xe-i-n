/**
 * Staff Payments Management
 * Xử lý thanh toán tại chỗ cho nhân viên
 */

import { API_BASE_URL } from './data.js';
import { fetchWithAuth } from './utils.js';

/**
 * Show payment confirmation modal
 */
export function showPaymentModal(sessionId) {
    // Fetch session details first
    fetchSessionDetails(sessionId).then(session => {
        if (!session) return;

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3>Xác nhận thanh toán</h3>
                    <button class="btn-close" onclick="this.closest('.modal-overlay').remove()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="payment-summary">
                        <h4>Thông tin phiên sạc</h4>
                        <table class="summary-table">
                            <tr>
                                <td>Khách hàng:</td>
                                <td><strong>${session.userName}</strong></td>
                            </tr>
                            <tr>
                                <td>Charger:</td>
                                <td><strong>${session.chargerName}</strong></td>
                            </tr>
                            <tr>
                                <td>Thời gian sạc:</td>
                                <td><strong>${calculateDuration(session.startTime, session.endTime)}</strong></td>
                            </tr>
                            <tr>
                                <td>Năng lượng tiêu thụ:</td>
                                <td><strong>${session.energyConsumed?.toFixed(2)} kWh</strong></td>
                            </tr>
                            <tr>
                                <td>Đơn giá:</td>
                                <td><strong>${formatCurrency(session.pricePerKwh)}/kWh</strong></td>
                            </tr>
                            <tr class="total-row">
                                <td>Tổng chi phí:</td>
                                <td><strong style="color: #1a73e8; font-size: 1.5em;">${formatCurrency(session.totalCost)}</strong></td>
                            </tr>
                        </table>
                    </div>

                    <div class="form-group">
                        <label for="paymentMethod">Phương thức thanh toán:</label>
                        <select id="paymentMethod" required>
                            <option value="">-- Chọn phương thức --</option>
                            <option value="CASH">Tiền mặt</option>
                            <option value="CARD">Thẻ ngân hàng</option>
                            <option value="MOMO">MoMo</option>
                            <option value="ZALOPAY">ZaloPay</option>
                            <option value="VNPAY">VNPay</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="amountReceived">Số tiền nhận (VND):</label>
                        <input type="number" id="amountReceived" value="${session.totalCost}" min="${session.totalCost}" step="1000" required />
                        <small style="color: #666;">Nhập số tiền lớn hơn hoặc bằng tổng chi phí</small>
                    </div>

                    <div class="form-group">
                        <label for="paymentNotes">Ghi chú (tùy chọn):</label>
                        <textarea id="paymentNotes" rows="2" placeholder="Ví dụ: Khách trả tiền mặt..."></textarea>
                    </div>

                    <div id="changeAmount" style="margin-top: 16px; padding: 12px; background: #f0f7ff; border-radius: 8px; display: none;">
                        <strong>Tiền thừa trả khách:</strong> <span id="changeValue" style="color: #1a73e8; font-size: 1.2em;"></span>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">
                        Hủy
                    </button>
                    <button class="btn btn-primary" onclick="staffPayments.confirmPaymentFromModal('${sessionId}')">
                        Xác nhận thanh toán
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // Add event listener for amount change
        const amountInput = document.getElementById('amountReceived');
        amountInput.addEventListener('input', () => {
            const received = parseFloat(amountInput.value) || 0;
            const total = session.totalCost;
            const change = received - total;

            const changeDiv = document.getElementById('changeAmount');
            const changeValue = document.getElementById('changeValue');

            if (change > 0) {
                changeDiv.style.display = 'block';
                changeValue.textContent = formatCurrency(change);
            } else {
                changeDiv.style.display = 'none';
            }
        });
    });
}

/**
 * Fetch session details
 */
async function fetchSessionDetails(sessionId) {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/charging/sessions/${sessionId}`);
        
        if (response.ok) {
            return await response.json();
        } else {
            showError('Không thể tải thông tin phiên sạc');
            return null;
        }
    } catch (error) {
        console.error('Error fetching session:', error);
        showError('Lỗi kết nối đến máy chủ');
        return null;
    }
}

/**
 * Confirm payment from modal
 */
export async function confirmPaymentFromModal(sessionId) {
    const method = document.getElementById('paymentMethod').value;
    const amountReceived = parseFloat(document.getElementById('amountReceived').value);
    const notes = document.getElementById('paymentNotes').value;

    // Validation
    if (!method) {
        showError('Vui lòng chọn phương thức thanh toán');
        return;
    }

    if (isNaN(amountReceived) || amountReceived <= 0) {
        showError('Số tiền không hợp lệ');
        return;
    }

    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/staff/payments/confirm`, {
            method: 'POST',
            body: JSON.stringify({
                sessionId,
                paymentMethod: method,
                amountReceived,
                notes
            })
        });

        if (response.ok) {
            const data = await response.json();
            
            // Close modal
            const modal = document.querySelector('.modal-overlay');
            if (modal) modal.remove();

            // Show success
            showSuccess('Thanh toán thành công!');

            // Show receipt option
            if (confirm('In hóa đơn cho khách hàng?')) {
                printReceipt(data.payment);
            }

            // Trigger refresh if needed
            if (window.staffSessions && window.staffSessions.loadActiveSessions) {
                const stationId = getCurrentStationId();
                if (stationId) {
                    window.staffSessions.loadActiveSessions(stationId);
                }
            }
        } else {
            const error = await response.json();
            showError(error.message || 'Không thể xác nhận thanh toán');
        }
    } catch (error) {
        console.error('Error confirming payment:', error);
        showError('Lỗi kết nối đến máy chủ');
    }
}

/**
 * Print receipt
 */
function printReceipt(payment) {
    const receiptWindow = window.open('', '_blank', 'width=400,height=600');
    
    receiptWindow.document.write(`
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Hóa đơn thanh toán</title>
            <style>
                body {
                    font-family: 'Courier New', monospace;
                    padding: 20px;
                    max-width: 350px;
                    margin: 0 auto;
                }
                .header {
                    text-align: center;
                    margin-bottom: 20px;
                    border-bottom: 2px dashed #000;
                    padding-bottom: 10px;
                }
                .header h2 {
                    margin: 5px 0;
                }
                .row {
                    display: flex;
                    justify-content: space-between;
                    margin: 8px 0;
                }
                .total {
                    border-top: 2px dashed #000;
                    margin-top: 15px;
                    padding-top: 10px;
                    font-weight: bold;
                    font-size: 1.2em;
                }
                .footer {
                    text-align: center;
                    margin-top: 20px;
                    border-top: 2px dashed #000;
                    padding-top: 10px;
                }
                @media print {
                    .no-print { display: none; }
                }
            </style>
        </head>
        <body>
            <div class="header">
                <h2>EV CHARGING STATION</h2>
                <p>HÓA ĐƠN THANH TOÁN</p>
                <small>Mã GD: ${payment.transactionId || payment.id}</small>
            </div>

            <div class="row">
                <span>Ngày:</span>
                <span>${new Date(payment.paymentTime).toLocaleString('vi-VN')}</span>
            </div>
            <div class="row">
                <span>Khách hàng:</span>
                <span>${payment.userName}</span>
            </div>
            <div class="row">
                <span>Trạm sạc:</span>
                <span>${payment.stationName}</span>
            </div>
            <div class="row">
                <span>Charger:</span>
                <span>${payment.chargerName}</span>
            </div>

            <div style="margin: 15px 0; border-top: 1px solid #000; border-bottom: 1px solid #000; padding: 10px 0;">
                <div class="row">
                    <span>Năng lượng:</span>
                    <span>${payment.energyConsumed?.toFixed(2)} kWh</span>
                </div>
                <div class="row">
                    <span>Đơn giá:</span>
                    <span>${formatCurrency(payment.pricePerKwh)}/kWh</span>
                </div>
                <div class="row">
                    <span>Thời gian:</span>
                    <span>${payment.duration}</span>
                </div>
            </div>

            <div class="total">
                <div class="row">
                    <span>TỔNG CỘNG:</span>
                    <span>${formatCurrency(payment.totalCost)}</span>
                </div>
            </div>

            <div style="margin-top: 10px;">
                <div class="row">
                    <span>Thanh toán:</span>
                    <span>${getPaymentMethodText(payment.paymentMethod)}</span>
                </div>
                <div class="row">
                    <span>Tiền nhận:</span>
                    <span>${formatCurrency(payment.amountReceived)}</span>
                </div>
                ${payment.changeAmount > 0 ? `
                <div class="row">
                    <span>Tiền thừa:</span>
                    <span>${formatCurrency(payment.changeAmount)}</span>
                </div>
                ` : ''}
            </div>

            <div class="footer">
                <p>Cảm ơn quý khách!</p>
                <p>Hẹn gặp lại!</p>
                <button class="no-print" onclick="window.print()" style="margin-top: 20px; padding: 10px 20px; cursor: pointer;">
                    In hóa đơn
                </button>
            </div>
        </body>
        </html>
    `);

    receiptWindow.document.close();
}

/**
 * Get payment method text
 */
function getPaymentMethodText(method) {
    const map = {
        'CASH': 'Tiền mặt',
        'CARD': 'Thẻ ngân hàng',
        'MOMO': 'MoMo',
        'ZALOPAY': 'ZaloPay',
        'VNPAY': 'VNPay',
        'WALLET': 'Ví điện tử'
    };
    return map[method] || method;
}

// Utility functions
function calculateDuration(startTime, endTime) {
    const start = new Date(startTime);
    const end = endTime ? new Date(endTime) : new Date();
    const diffMs = end - start;
    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    return `${hours}h ${minutes}m`;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function getCurrentStationId() {
    return localStorage.getItem('staffStationId') || document.getElementById('stationSelector')?.value;
}

function showSuccess(message) {
    if (window.showNotification) {
        window.showNotification(message, 'success');
    } else {
        alert(message);
    }
}

function showError(message) {
    if (window.showNotification) {
        window.showNotification(message, 'error');
    } else {
        alert(message);
    }
}

// Export for global access
window.staffPayments = {
    showPaymentModal,
    confirmPaymentFromModal
};
