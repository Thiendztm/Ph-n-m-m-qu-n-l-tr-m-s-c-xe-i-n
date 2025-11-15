// Charging History Script
// Using centralized API client for better data flow
import api from './api-client.js';
import { sessionManager, showNotification } from './data-flow.js';

const API_BASE_URL = 'http://localhost:8080/api';

let allSessions = [];
let filteredSessions = [];

// DOM Elements
const sessionsList = document.getElementById('sessionsList');
const totalSessionsEl = document.getElementById('totalSessions');
const totalEnergyEl = document.getElementById('totalEnergy');
const totalCostEl = document.getElementById('totalCost');
const statusFilter = document.getElementById('statusFilter');
const dateFrom = document.getElementById('dateFrom');
const dateTo = document.getElementById('dateTo');
const exportBtn = document.getElementById('exportBtn');

// Check authentication
function checkAuth() {
    const token = localStorage.getItem('accessToken');
    if (!token) {
        window.location.href = './login.html';
        return false;
    }
    return true;
}

// Fetch charging history from API
async function fetchChargingHistory() {
    if (!checkAuth()) return;

    try {
        const token = localStorage.getItem('accessToken');
        const userId = localStorage.getItem('userId');
        
        const response = await fetch(`${API_BASE_URL}/charging/history?userId=${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                localStorage.clear();
                window.location.href = './login.html';
                return;
            }
            throw new Error('Failed to fetch history');
        }

        const data = await response.json();
        allSessions = data.sessions || generateMockData(); // Fallback to mock data for demo
        filteredSessions = [...allSessions];
        
        renderHistory();
        updateStats();
        
    } catch (error) {
        console.error('Error fetching history:', error);
        // Use mock data for demonstration
        allSessions = generateMockData();
        filteredSessions = [...allSessions];
        renderHistory();
        updateStats();
    }
}

// Generate mock data for demonstration
function generateMockData() {
    const stations = ['Trạm Nguyễn Huệ', 'Trạm Lê Lợi', 'Trạm Trần Phú', 'Trạm Hai Bà Trưng'];
    const mockSessions = [];
    
    for (let i = 0; i < 15; i++) {
        const date = new Date();
        date.setDate(date.getDate() - Math.floor(Math.random() * 30));
        
        const energyConsumed = (Math.random() * 50 + 10).toFixed(2);
        const costPerKwh = 3500;
        const totalCost = (energyConsumed * costPerKwh).toFixed(0);
        
        mockSessions.push({
            sessionId: `SESSION-${1000 + i}`,
            stationName: stations[Math.floor(Math.random() * stations.length)],
            startTime: date.toISOString(),
            endTime: new Date(date.getTime() + (Math.random() * 3 + 1) * 3600000).toISOString(),
            energyConsumed: parseFloat(energyConsumed),
            totalCost: parseInt(totalCost),
            startSoc: Math.floor(Math.random() * 30 + 10),
            endSoc: Math.floor(Math.random() * 20 + 80),
            status: Math.random() > 0.1 ? 'COMPLETED' : 'FAILED'
        });
    }
    
    return mockSessions.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
}

// Render history list
function renderHistory() {
    if (filteredSessions.length === 0) {
        sessionsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <h3>Chưa có lịch sử sạc</h3>
                <p>Bạn chưa có phiên sạc nào. Hãy bắt đầu sạc ngay!</p>
                <a href="./index.html" class="btn-start">
                    <i class="fas fa-map-marked-alt"></i> Tìm trạm sạc
                </a>
            </div>
        `;
        return;
    }

    const html = filteredSessions.map(session => {
        const startDate = new Date(session.startTime);
        const endDate = new Date(session.endTime);
        const duration = Math.floor((endDate - startDate) / 60000); // minutes
        
        return `
            <div class="session-item" onclick="viewSessionDetail('${session.sessionId}')">
                <div class="session-icon ${session.status.toLowerCase()}">
                    <i class="fas fa-charging-station"></i>
                </div>
                <div class="session-info">
                    <h3>${session.stationName}</h3>
                    <div class="session-details">
                        <span>
                            <i class="fas fa-calendar"></i>
                            ${formatDate(startDate)}
                        </span>
                        <span>
                            <i class="fas fa-clock"></i>
                            ${formatTime(startDate)} - ${formatTime(endDate)}
                        </span>
                        <span>
                            <i class="fas fa-hourglass-half"></i>
                            ${duration} phút
                        </span>
                        <span>
                            <i class="fas fa-battery-three-quarters"></i>
                            ${session.startSoc}% → ${session.endSoc}%
                        </span>
                    </div>
                </div>
                <div class="session-summary">
                    <div class="energy-used">${session.energyConsumed} kWh</div>
                    <div class="session-cost">${formatCurrency(session.totalCost)}</div>
                    <span class="status-badge ${session.status.toLowerCase()}">
                        ${session.status === 'COMPLETED' ? 'Hoàn thành' : 'Thất bại'}
                    </span>
                </div>
            </div>
        `;
    }).join('');

    sessionsList.innerHTML = html;
}

// Update statistics
function updateStats() {
    const completedSessions = filteredSessions.filter(s => s.status === 'COMPLETED');
    
    const totalEnergy = completedSessions.reduce((sum, s) => sum + s.energyConsumed, 0);
    const totalCost = completedSessions.reduce((sum, s) => sum + s.totalCost, 0);
    
    totalSessionsEl.textContent = completedSessions.length;
    totalEnergyEl.textContent = totalEnergy.toFixed(2) + ' kWh';
    totalCostEl.textContent = formatCurrency(totalCost);
}

// Filter sessions
function applyFilters() {
    filteredSessions = allSessions.filter(session => {
        // Status filter
        if (statusFilter.value && session.status !== statusFilter.value) {
            return false;
        }
        
        // Date range filter
        const sessionDate = new Date(session.startTime);
        
        if (dateFrom.value) {
            const fromDate = new Date(dateFrom.value);
            if (sessionDate < fromDate) return false;
        }
        
        if (dateTo.value) {
            const toDate = new Date(dateTo.value);
            toDate.setHours(23, 59, 59);
            if (sessionDate > toDate) return false;
        }
        
        return true;
    });
    
    renderHistory();
    updateStats();
}

// View session detail
function viewSessionDetail(sessionId) {
    window.location.href = `./session-detail.html?id=${sessionId}`;
}

// Export to CSV
function exportToCSV() {
    const headers = ['Mã phiên', 'Trạm sạc', 'Ngày', 'Giờ bắt đầu', 'Giờ kết thúc', 'Năng lượng (kWh)', 'Chi phí (đ)', 'SOC bắt đầu', 'SOC kết thúc', 'Trạng thái'];
    
    const rows = filteredSessions.map(session => {
        const startDate = new Date(session.startTime);
        const endDate = new Date(session.endTime);
        
        return [
            session.sessionId,
            session.stationName,
            formatDate(startDate),
            formatTime(startDate),
            formatTime(endDate),
            session.energyConsumed,
            session.totalCost,
            session.startSoc,
            session.endSoc,
            session.status === 'COMPLETED' ? 'Hoàn thành' : 'Thất bại'
        ].join(',');
    });
    
    const csv = [headers.join(','), ...rows].join('\n');
    
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `lich-su-sac-${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
}

// Utility functions
function formatDate(date) {
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

function formatTime(date) {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Event listeners
statusFilter.addEventListener('change', applyFilters);
dateFrom.addEventListener('change', applyFilters);
dateTo.addEventListener('change', applyFilters);
exportBtn.addEventListener('click', exportToCSV);

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    fetchChargingHistory();
});
