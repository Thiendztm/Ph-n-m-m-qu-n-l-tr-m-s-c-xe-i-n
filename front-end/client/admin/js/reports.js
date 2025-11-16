/**
 * Admin Reports & Analytics
 * Revenue charts, energy statistics, user growth
 */

import { API_BASE_URL, fetchWithAuth } from './api-client.js';
import { showNotification } from './utils.js';

let revenueChart = null;
let energyChart = null;
let userGrowthChart = null;

/**
 * Initialize reports page
 */
export function initReports() {
    renderReportsPage();
    loadDashboardData();
}

/**
 * Render the reports page
 */
function renderReportsPage() {
    const content = document.querySelector('.main-content');
    if (!content) {
        console.error('Main content container not found');
        return;
    }
    
    content.innerHTML = `
        <div class="page-header">
            <h2>Báo cáo & Thống kê</h2>
            <div class="date-range-selector">
                <select id="timeRange" onchange="reportsModule.changeTimeRange(this.value)">
                    <option value="7">7 ngày qua</option>
                    <option value="30" selected>30 ngày qua</option>
                    <option value="90">90 ngày qua</option>
                    <option value="365">1 năm qua</option>
                </select>
            </div>
        </div>

        <!-- Summary Cards -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon revenue">
                    <i class="fas fa-dollar-sign"></i>
                </div>
                <div class="stat-content">
                    <div class="stat-label">Tổng doanh thu</div>
                    <div class="stat-value" id="totalRevenue">0 ₫</div>
                    <div class="stat-change positive" id="revenueChange">+0%</div>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon energy">
                    <i class="fas fa-bolt"></i>
                </div>
                <div class="stat-content">
                    <div class="stat-label">Năng lượng tiêu thụ</div>
                    <div class="stat-value" id="totalEnergy">0 kWh</div>
                    <div class="stat-change positive" id="energyChange">+0%</div>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon sessions">
                    <i class="fas fa-charging-station"></i>
                </div>
                <div class="stat-content">
                    <div class="stat-label">Số phiên sạc</div>
                    <div class="stat-value" id="totalSessions">0</div>
                    <div class="stat-change positive" id="sessionsChange">+0%</div>
                </div>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon users">
                    <i class="fas fa-users"></i>
                </div>
                <div class="stat-content">
                    <div class="stat-label">Người dùng mới</div>
                    <div class="stat-value" id="newUsers">0</div>
                    <div class="stat-change positive" id="usersChange">+0%</div>
                </div>
            </div>
        </div>

        <!-- Charts Grid -->
        <div class="charts-grid">
            <div class="chart-card">
                <div class="chart-header">
                    <h3>Doanh thu theo ngày</h3>
                    <button class="btn btn-sm btn-outline" onclick="reportsModule.exportRevenueData()">
                        <i class="fas fa-download"></i> Xuất CSV
                    </button>
                </div>
                <canvas id="revenueChart"></canvas>
            </div>

            <div class="chart-card">
                <div class="chart-header">
                    <h3>Năng lượng tiêu thụ</h3>
                    <button class="btn btn-sm btn-outline" onclick="reportsModule.exportEnergyData()">
                        <i class="fas fa-download"></i> Xuất CSV
                    </button>
                </div>
                <canvas id="energyChart"></canvas>
            </div>
        </div>

        <div class="charts-grid">
            <div class="chart-card">
                <div class="chart-header">
                    <h3>Tăng trưởng người dùng</h3>
                </div>
                <canvas id="userGrowthChart"></canvas>
            </div>

            <div class="chart-card">
                <div class="chart-header">
                    <h3>Top trạm sạc</h3>
                </div>
                <div id="topStationsList" class="top-list">
                    <div class="loading">
                        <i class="fas fa-spinner fa-spin"></i> Đang tải...
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Sessions Table -->
        <div class="chart-card">
            <div class="chart-header">
                <h3>Phiên sạc gần đây</h3>
            </div>
            <div class="table-container">
                <table class="sessions-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Trạm sạc</th>
                            <th>Người dùng</th>
                            <th>Thời gian</th>
                            <th>Năng lượng</th>
                            <th>Doanh thu</th>
                            <th>Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody id="recentSessionsBody">
                        <tr>
                            <td colspan="7" style="text-align: center; padding: 40px;">
                                <i class="fas fa-spinner fa-spin"></i> Đang tải...
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    `;

    // Load Chart.js if not already loaded
    loadChartJS();
}

/**
 * Load Chart.js library
 */
function loadChartJS() {
    if (window.Chart) {
        return; // Already loaded
    }

    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js';
    script.onload = () => {
        loadDashboardData();
    };
    document.head.appendChild(script);
}

/**
 * Load dashboard data
 */
async function loadDashboardData() {
    const days = parseInt(document.getElementById('timeRange')?.value || '30');
    
    try {
        // Load summary stats
        const summaryResponse = await fetchWithAuth(`${API_BASE_URL}/admin/reports/summary?days=${days}`);
        if (summaryResponse.ok) {
            const summary = await summaryResponse.json();
            updateSummaryCards(summary);
        }

        // Load revenue data
        const revenueResponse = await fetchWithAuth(`${API_BASE_URL}/admin/reports/revenue?days=${days}`);
        if (revenueResponse.ok) {
            const revenueData = await revenueResponse.json();
            renderRevenueChart(revenueData);
        }

        // Load energy data
        const energyResponse = await fetchWithAuth(`${API_BASE_URL}/admin/reports/energy?days=${days}`);
        if (energyResponse.ok) {
            const energyData = await energyResponse.json();
            renderEnergyChart(energyData);
        }

        // Load user growth data
        const userGrowthResponse = await fetchWithAuth(`${API_BASE_URL}/admin/reports/user-growth?days=${days}`);
        if (userGrowthResponse.ok) {
            const userGrowthData = await userGrowthResponse.json();
            renderUserGrowthChart(userGrowthData);
        }

        // Load top stations
        const topStationsResponse = await fetchWithAuth(`${API_BASE_URL}/admin/reports/top-stations?days=${days}`);
        if (topStationsResponse.ok) {
            const topStations = await topStationsResponse.json();
            renderTopStations(topStations);
        }

        // Load recent sessions
        const sessionsResponse = await fetchWithAuth(`${API_BASE_URL}/admin/reports/recent-sessions?limit=10`);
        if (sessionsResponse.ok) {
            const sessions = await sessionsResponse.json();
            renderRecentSessions(sessions);
        }

    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showNotification('Lỗi tải dữ liệu báo cáo', 'error');
    }
}

/**
 * Update summary cards
 */
function updateSummaryCards(summary) {
    document.getElementById('totalRevenue').textContent = formatCurrency(summary.totalRevenue || 0);
    document.getElementById('totalEnergy').textContent = `${(summary.totalEnergy || 0).toFixed(2)} kWh`;
    document.getElementById('totalSessions').textContent = summary.totalSessions || 0;
    document.getElementById('newUsers').textContent = summary.newUsers || 0;

    // Update change indicators
    updateChangeIndicator('revenueChange', summary.revenueChange);
    updateChangeIndicator('energyChange', summary.energyChange);
    updateChangeIndicator('sessionsChange', summary.sessionsChange);
    updateChangeIndicator('usersChange', summary.usersChange);
}

/**
 * Update change indicator
 */
function updateChangeIndicator(elementId, changePercent) {
    const element = document.getElementById(elementId);
    if (!element) return;

    const isPositive = changePercent >= 0;
    element.className = `stat-change ${isPositive ? 'positive' : 'negative'}`;
    element.innerHTML = `<i class="fas fa-${isPositive ? 'arrow-up' : 'arrow-down'}"></i> ${Math.abs(changePercent).toFixed(1)}%`;
}

/**
 * Render revenue chart
 */
function renderRevenueChart(data) {
    const ctx = document.getElementById('revenueChart');
    if (!ctx) return;

    // Destroy existing chart
    if (revenueChart) {
        revenueChart.destroy();
    }

    revenueChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.labels || [],
            datasets: [{
                label: 'Doanh thu (VND)',
                data: data.values || [],
                borderColor: '#4f46e5',
                backgroundColor: 'rgba(79, 70, 229, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return formatCurrency(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatShortCurrency(value);
                        }
                    }
                }
            }
        }
    });
}

/**
 * Render energy chart
 */
function renderEnergyChart(data) {
    const ctx = document.getElementById('energyChart');
    if (!ctx) return;

    // Destroy existing chart
    if (energyChart) {
        energyChart.destroy();
    }

    energyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.labels || [],
            datasets: [{
                label: 'Năng lượng (kWh)',
                data: data.values || [],
                backgroundColor: '#10b981',
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

/**
 * Render user growth chart
 */
function renderUserGrowthChart(data) {
    const ctx = document.getElementById('userGrowthChart');
    if (!ctx) return;

    // Destroy existing chart
    if (userGrowthChart) {
        userGrowthChart.destroy();
    }

    userGrowthChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.labels || [],
            datasets: [{
                label: 'Người dùng mới',
                data: data.values || [],
                borderColor: '#f59e0b',
                backgroundColor: 'rgba(245, 158, 11, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}

/**
 * Render top stations
 */
function renderTopStations(stations) {
    const container = document.getElementById('topStationsList');
    
    if (!stations || stations.length === 0) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-inbox"></i><p>Không có dữ liệu</p></div>';
        return;
    }

    container.innerHTML = stations.map((station, index) => `
        <div class="top-item">
            <div class="rank">#${index + 1}</div>
            <div class="item-info">
                <div class="item-name">${station.name}</div>
                <div class="item-value">${formatCurrency(station.revenue)}</div>
            </div>
            <div class="item-meta">
                <small>${station.sessionCount} phiên</small>
            </div>
        </div>
    `).join('');
}

/**
 * Render recent sessions
 */
function renderRecentSessions(sessions) {
    const tbody = document.getElementById('recentSessionsBody');
    
    if (!sessions || sessions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 40px; color: #666;">
                    Không có phiên sạc gần đây
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = sessions.map(session => `
        <tr>
            <td>${session.id}</td>
            <td>${session.stationName}</td>
            <td>${session.userName}</td>
            <td>${formatDateTime(session.startTime)}</td>
            <td>${session.energyConsumed?.toFixed(2) || '0.00'} kWh</td>
            <td>${formatCurrency(session.totalCost || 0)}</td>
            <td>
                <span class="status-badge status-${session.status.toLowerCase()}">
                    ${getSessionStatusText(session.status)}
                </span>
            </td>
        </tr>
    `).join('');
}

/**
 * Change time range
 */
export function changeTimeRange(days) {
    loadDashboardData();
}

/**
 * Export revenue data to CSV
 */
export function exportRevenueData() {
    // Get chart data
    if (!revenueChart) {
        showNotification('Không có dữ liệu để xuất', 'warning');
        return;
    }

    const data = revenueChart.data;
    let csv = 'Ngày,Doanh thu (VND)\n';
    
    data.labels.forEach((label, index) => {
        csv += `${label},${data.datasets[0].data[index]}\n`;
    });

    downloadCSV(csv, 'revenue_report.csv');
    showNotification('Đã xuất dữ liệu doanh thu', 'success');
}

/**
 * Export energy data to CSV
 */
export function exportEnergyData() {
    if (!energyChart) {
        showNotification('Không có dữ liệu để xuất', 'warning');
        return;
    }

    const data = energyChart.data;
    let csv = 'Ngày,Năng lượng (kWh)\n';
    
    data.labels.forEach((label, index) => {
        csv += `${label},${data.datasets[0].data[index]}\n`;
    });

    downloadCSV(csv, 'energy_report.csv');
    showNotification('Đã xuất dữ liệu năng lượng', 'success');
}

/**
 * Download CSV file
 */
function downloadCSV(content, filename) {
    const blob = new Blob(['\ufeff' + content], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    link.click();
}

// Utility functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatShortCurrency(amount) {
    if (amount >= 1000000) {
        return (amount / 1000000).toFixed(1) + 'M';
    } else if (amount >= 1000) {
        return (amount / 1000).toFixed(1) + 'K';
    }
    return amount.toString();
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getSessionStatusText(status) {
    const map = {
        'ACTIVE': 'Đang sạc',
        'COMPLETED': 'Hoàn thành',
        'STOPPED': 'Đã dừng',
        'ERROR': 'Lỗi'
    };
    return map[status] || status;
}

// Export for global access
window.reportsModule = {
    initReports,
    changeTimeRange,
    exportRevenueData,
    exportEnergyData
};
