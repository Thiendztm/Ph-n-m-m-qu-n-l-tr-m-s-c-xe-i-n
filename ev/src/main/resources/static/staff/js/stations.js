/**
 * Station monitoring and management for Staff Dashboard
 */

import { API_BASE_URL, state, setStations, setCurrentStation } from './data.js';
import { fetchWithAuth, formatCurrency, showNotification } from './utils.js';

export async function loadStations() {
  try {
    const response = await fetchWithAuth(`${API_BASE_URL}/staff/stations`);
    const data = await response.json();
    
    if (data.success) {
      setStations(data.stations);
      renderStationsSelect(data.stations);
    } else {
      showNotification('Không thể tải danh sách trạm sạc', 'error');
    }
  } catch (error) {
    console.error('Error loading stations:', error);
    showNotification('Lỗi khi tải danh sách trạm sạc', 'error');
  }
}

export async function loadStationStatus(stationId) {
  try {
    const response = await fetchWithAuth(`${API_BASE_URL}/staff/station/${stationId}/status`);
    const data = await response.json();
    
    if (data.success) {
      renderStationStatus(data);
    } else {
      showNotification('Không thể tải trạng thái trạm sạc', 'error');
    }
  } catch (error) {
    console.error('Error loading station status:', error);
    showNotification('Lỗi khi tải trạng thái trạm sạc', 'error');
  }
}

function renderStationsSelect(stations) {
  const select = document.getElementById('stationSelect');
  if (!select) return;

  select.innerHTML = '<option value="">-- Chọn trạm sạc --</option>';
  
  stations.forEach(station => {
    const option = document.createElement('option');
    option.value = station.id;
    option.textContent = `${station.name} (${station.availableChargers}/${station.totalChargers} khả dụng)`;
    select.appendChild(option);
  });

  select.addEventListener('change', (e) => {
    const stationId = e.target.value;
    if (stationId) {
      setCurrentStation(stationId);
      loadStationStatus(stationId);
    }
  });
}

function renderStationStatus(data) {
  const content = document.getElementById('monitoringContent');
  if (!content) return;

  const { station, summary, chargers, activeSessions } = data;

  content.innerHTML = `
    <div class="station-header">
      <h2>${station.name}</h2>
      <p>${station.address}</p>
      <span class="status-badge ${station.status.toLowerCase()}">${station.status}</span>
    </div>

    <div class="summary-cards">
      <div class="card">
        <h3>Tổng điểm sạc</h3>
        <p class="big-number">${summary.totalChargers}</p>
      </div>
      <div class="card success">
        <h3>Khả dụng</h3>
        <p class="big-number">${summary.availableChargers}</p>
      </div>
      <div class="card warning">
        <h3>Đang sử dụng</h3>
        <p class="big-number">${summary.occupiedChargers}</p>
      </div>
      <div class="card error">
        <h3>Hỏng hóc</h3>
        <p class="big-number">${summary.outOfOrderChargers}</p>
      </div>
    </div>

    <div class="chargers-grid">
      <h3>Danh sách điểm sạc</h3>
      <div class="chargers-list">
        ${chargers.map(charger => `
          <div class="charger-card status-${charger.status.toLowerCase()}">
            <h4>${charger.name}</h4>
            <p>${charger.connectorType} - ${charger.powerCapacity}kW</p>
            <p>${formatCurrency(charger.pricePerKwh)}/kWh</p>
            <span class="status">${charger.status}</span>
          </div>
        `).join('')}
      </div>
    </div>

    <div class="active-sessions">
      <h3>Phiên sạc đang hoạt động (${activeSessions.length})</h3>
      <table class="data-table">
        <thead>
          <tr>
            <th>Điểm sạc</th>
            <th>Thời gian bắt đầu</th>
            <th>Năng lượng (kWh)</th>
            <th>Chi phí</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          ${activeSessions.map(session => `
            <tr>
              <td>${session.chargerName}</td>
              <td>${new Date(session.startTime).toLocaleString('vi-VN')}</td>
              <td>${session.energyConsumed || 0} kWh</td>
              <td>${formatCurrency(session.currentCost || 0)}</td>
              <td>
                <button class="btn btn-sm" onclick="viewSessionDetails(${session.sessionId})">
                  Chi tiết
                </button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

// Make function available globally for onclick handlers
window.viewSessionDetails = function(sessionId) {
  alert('Chi tiết phiên sạc #' + sessionId + ' sẽ được hiển thị ở đây');
  // TODO: Implement session details modal
};
