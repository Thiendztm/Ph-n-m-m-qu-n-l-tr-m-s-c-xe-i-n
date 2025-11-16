// Session Detail Script
const API_BASE_URL = window.API_BASE_URL || 'http://localhost:8080/api';

// Get session ID from URL or localStorage fallback
const urlParams = new URLSearchParams(window.location.search);
let sessionId = urlParams.get('id') 
    || localStorage.getItem('currentSessionId') 
    || localStorage.getItem('currentBookingId');

if (!sessionId) {
    window.location.href = './charging-history.html';
}

// Fetch session detail
async function fetchSessionDetail() {
    try {
        const token = localStorage.getItem('accessToken');
        
        const response = await fetch(`${API_BASE_URL}/charging/session/${sessionId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 404) {
                showNotFoundError();
                return;
            }
            throw new Error('Failed to fetch session detail');
        }

        const session = await response.json();
        renderSessionDetail(session);
        
    } catch (error) {
        console.error('Error:', error);
        showErrorState();
    }
}

// Show error state when session not found
function showNotFoundError() {
    document.getElementById('sessionDetail').innerHTML = `
        <div class="session-card">
            <div class="card-body" style="text-align: center; padding: 48px 24px;">
                <i class="fas fa-exclamation-circle" style="font-size: 64px; color: #ff6b6b; margin-bottom: 16px;"></i>
                <h2 style="margin-bottom: 8px;">Không tìm thấy phiên sạc</h2>
                <p style="opacity: 0.7; margin-bottom: 24px;">Phiên sạc này không tồn tại hoặc đã bị xóa</p>
                <button class="btn btn-primary" onclick="window.location.href='./charging-history.html'">
                    <i class="fas fa-arrow-left"></i>
                    Quay lại lịch sử
                </button>
            </div>
        </div>
    `;
}

// Show error state when API fails
function showErrorState() {
    document.getElementById('sessionDetail').innerHTML = `
        <div class="session-card">
            <div class="card-body" style="text-align: center; padding: 48px 24px;">
                <i class="fas fa-exclamation-triangle" style="font-size: 64px; color: #ffa500; margin-bottom: 16px;"></i>
                <h2 style="margin-bottom: 8px;">Lỗi tải dữ liệu</h2>
                <p style="opacity: 0.7; margin-bottom: 24px;">Có lỗi xảy ra khi tải thông tin phiên sạc</p>
                <button class="btn btn-primary" onclick="fetchSessionDetail()">
                    <i class="fas fa-sync-alt"></i>
                    Thử lại
                </button>
                <button class="btn btn-outline" onclick="window.location.href='./charging-history.html'" style="margin-left: 12px;">
                    <i class="fas fa-arrow-left"></i>
                    Quay lại
                </button>
            </div>
        </div>
    `;
}

// Render session detail
function renderSessionDetail(session) {
    const startDate = new Date(session.startTime);
    const endDate = new Date(session.endTime);
    const duration = Math.floor((endDate - startDate) / 60000); // minutes
    const hours = Math.floor(duration / 60);
    const minutes = duration % 60;
    
    const socGain = session.endSoc - session.startSoc;
    
    const html = `
        <div class="session-card">
            <div class="card-header">
                <div class="status-icon">
                    <i class="fas fa-${session.status === 'COMPLETED' ? 'check-circle' : 'times-circle'}"></i>
                </div>
                <h1>${session.stationName}</h1>
                <div class="session-id">${session.sessionId}</div>
                <span class="status-badge-large">
                    ${session.status === 'COMPLETED' ? 'Hoàn thành' : 'Thất bại'}
                </span>
            </div>

            <div class="card-body">
                <!-- Progress Section -->
                <div class="progress-section">
                    <div class="soc-progress">
                        <div>
                            <div style="font-size: 14px; opacity: 0.9; margin-bottom: 4px;">Bắt đầu</div>
                            <div class="soc-value">${session.startSoc}%</div>
                        </div>
                        <div class="progress-arrow">
                            <i class="fas fa-arrow-right"></i>
                        </div>
                        <div>
                            <div style="font-size: 14px; opacity: 0.9; margin-bottom: 4px;">Kết thúc</div>
                            <div class="soc-value">${session.endSoc}%</div>
                        </div>
                    </div>
                    <div class="progress-bar-container">
                        <div class="progress-bar" style="width: ${socGain}%"></div>
                    </div>
                    <div style="text-align: center; margin-top: 12px; font-size: 14px;">
                        Tăng ${socGain}% pin
                    </div>
                </div>

                <!-- Info Grid -->
                <div class="info-grid">
                    <div class="info-item">
                        <div class="info-label">
                            <i class="fas fa-bolt"></i>
                            Năng lượng
                        </div>
                        <div class="info-value highlight">${session.energyConsumed} kWh</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">
                            <i class="fas fa-clock"></i>
                            Thời gian
                        </div>
                        <div class="info-value">${hours}h ${minutes}m</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">
                            <i class="fas fa-tachometer-alt"></i>
                            Công suất
                        </div>
                        <div class="info-value">${session.powerOutput} kW</div>
                    </div>
                </div>

                <!-- Timeline -->
                <div class="section-title">
                    <i class="fas fa-history"></i>
                    Thời gian sạc
                </div>
                <div class="timeline">
                    <div class="timeline-item">
                        <div class="timeline-dot">
                            <i class="fas fa-play"></i>
                        </div>
                        <div class="timeline-content">
                            <div class="timeline-time">${formatDateTime(startDate)}</div>
                            <div class="timeline-desc">Bắt đầu sạc - SOC: ${session.startSoc}%</div>
                        </div>
                    </div>
                    <div class="timeline-item">
                        <div class="timeline-dot success">
                            <i class="fas fa-stop"></i>
                        </div>
                        <div class="timeline-content">
                            <div class="timeline-time">${formatDateTime(endDate)}</div>
                            <div class="timeline-desc">Kết thúc sạc - SOC: ${session.endSoc}%</div>
                        </div>
                    </div>
                </div>

                <!-- Cost Breakdown -->
                <div class="section-title" style="margin-top: 32px;">
                    <i class="fas fa-receipt"></i>
                    Chi tiết thanh toán
                </div>
                <div class="cost-breakdown">
                    <div class="cost-row">
                        <span>Địa điểm</span>
                        <span style="font-weight: 600;">${session.stationAddress}</span>
                    </div>
                    <div class="cost-row">
                        <span>Năng lượng tiêu thụ</span>
                        <span style="font-weight: 600;">${session.energyConsumed} kWh</span>
                    </div>
                    <div class="cost-row">
                        <span>Đơn giá</span>
                        <span style="font-weight: 600;">${formatCurrency(session.costPerKwh)}/kWh</span>
                    </div>
                    <div class="cost-row">
                        <span>Phương thức thanh toán</span>
                        <span style="font-weight: 600;">${session.paymentMethod}</span>
                    </div>
                    <div class="cost-row">
                        <span>Tổng tiền</span>
                        <span>${formatCurrency(session.totalCost)}</span>
                    </div>
                </div>

                <!-- Action Buttons -->
                <div class="action-buttons">
                    <button class="btn btn-primary" onclick="downloadInvoice()">
                        <i class="fas fa-download"></i>
                        Tải hóa đơn
                    </button>
                    <button class="btn btn-outline" onclick="shareSession()">
                        <i class="fas fa-share-alt"></i>
                        Chia sẻ
                    </button>
                </div>
            </div>
        </div>
    `;

    document.getElementById('sessionDetail').innerHTML = html;
    
    // Animate progress bar
    setTimeout(() => {
        const progressBar = document.querySelector('.progress-bar');
        if (progressBar) {
            progressBar.style.width = `${socGain}%`;
        }
    }, 100);
}

// Utility functions
function formatDateTime(date) {
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes} - ${day}/${month}/${year}`;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Download invoice
function downloadInvoice() {
    alert('Chức năng tải hóa đơn đang được phát triển');
    // TODO: Implement invoice PDF generation
}

// Share session
function shareSession() {
    if (navigator.share) {
        navigator.share({
            title: 'Chi tiết phiên sạc',
            text: `Tôi vừa sạc xe tại EV Charging Station - Phiên ${sessionId}`,
            url: window.location.href
        });
    } else {
        // Fallback: copy to clipboard
        navigator.clipboard.writeText(window.location.href);
        alert('Đã copy link vào clipboard!');
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    fetchSessionDetail();
});
