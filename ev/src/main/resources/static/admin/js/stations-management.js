/**
 * Admin Stations Management
 * CRUD operations for stations and chargers
 */

import { API_BASE_URL, fetchWithAuth } from './api-client.js';
import { showNotification } from './utils.js';

let allStations = [];
let selectedStation = null;

/**
 * Initialize stations management page
 */
export function initStationsManagement() {
    renderStationsPage();
    loadStations();
}

/**
 * Render the stations management page
 */
function renderStationsPage() {
    const content = document.querySelector('.main-content');
    if (!content) {
        console.error('Main content container not found');
        return;
    }
    
    content.innerHTML = `
        <div class="page-header">
            <h2>Quản lý trạm sạc</h2>
            <button class="btn btn-primary" onclick="stationsManagement.showCreateStationModal()">
                <i class="fas fa-plus"></i> Thêm trạm mới
            </button>
        </div>

        <div class="stations-grid">
            <div class="stations-list-panel">
                <div class="search-box">
                    <input type="text" id="stationSearch" placeholder="Tìm kiếm trạm sạc..." />
                    <i class="fas fa-search"></i>
                </div>
                
                <div id="stationsList" class="stations-list">
                    <div class="loading">
                        <i class="fas fa-spinner fa-spin"></i> Đang tải...
                    </div>
                </div>
            </div>

            <div class="station-details-panel">
                <div id="stationDetails" class="empty-state">
                    <i class="fas fa-charging-station"></i>
                    <p>Chọn một trạm để xem chi tiết</p>
                </div>
            </div>
        </div>
    `;

    // Setup search
    document.getElementById('stationSearch')?.addEventListener('input', (e) => {
        filterStations(e.target.value);
    });
}

/**
 * Load all stations
 */
async function loadStations() {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/admin/stations`);
        
        if (response.ok) {
            allStations = await response.json();
            renderStationsList(allStations);
        } else {
            showNotification('Không thể tải danh sách trạm sạc', 'error');
        }
    } catch (error) {
        console.error('Error loading stations:', error);
        showNotification('Lỗi kết nối đến máy chủ', 'error');
    }
}

/**
 * Render stations list
 */
function renderStationsList(stations) {
    const listContainer = document.getElementById('stationsList');
    
    if (stations.length === 0) {
        listContainer.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>Không có trạm sạc nào</p>
            </div>
        `;
        return;
    }

    listContainer.innerHTML = stations.map(station => `
        <div class="station-card ${selectedStation?.id === station.id ? 'active' : ''}" 
             onclick="stationsManagement.selectStation(${station.id})">
            <div class="station-card-header">
                <h4>${station.name}</h4>
                <span class="status-badge ${station.status === 'ACTIVE' ? 'status-active' : 'status-inactive'}">
                    ${station.status === 'ACTIVE' ? 'Hoạt động' : 'Bảo trì'}
                </span>
            </div>
            <div class="station-card-body">
                <div class="info-row">
                    <i class="fas fa-map-marker-alt"></i>
                    <span>${station.address || 'N/A'}</span>
                </div>
                <div class="info-row">
                    <i class="fas fa-plug"></i>
                    <span>${station.totalChargers || 0} chargers</span>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * Filter stations by search term
 */
function filterStations(searchTerm) {
    const filtered = allStations.filter(station => 
        station.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        station.address?.toLowerCase().includes(searchTerm.toLowerCase())
    );
    renderStationsList(filtered);
}

/**
 * Select a station to view details
 */
export async function selectStation(stationId) {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/admin/stations/${stationId}`);
        
        if (response.ok) {
            selectedStation = await response.json();
            renderStationDetails(selectedStation);
            
            // Update active state in list
            document.querySelectorAll('.station-card').forEach(card => {
                card.classList.remove('active');
            });
            event?.currentTarget?.classList.add('active');
        } else {
            showNotification('Không thể tải thông tin trạm sạc', 'error');
        }
    } catch (error) {
        console.error('Error loading station details:', error);
        showNotification('Lỗi kết nối đến máy chủ', 'error');
    }
}

/**
 * Render station details
 */
function renderStationDetails(station) {
    const detailsContainer = document.getElementById('stationDetails');
    
    detailsContainer.innerHTML = `
        <div class="station-details">
            <div class="details-header">
                <div>
                    <h2>${station.name}</h2>
                    <p class="text-muted">${station.address || 'N/A'}</p>
                </div>
                <div class="actions">
                    <button class="btn btn-outline" onclick="stationsManagement.showEditStationModal(${station.id})">
                        <i class="fas fa-edit"></i> Sửa
                    </button>
                    <button class="btn btn-danger" onclick="stationsManagement.deleteStation(${station.id})">
                        <i class="fas fa-trash"></i> Xóa
                    </button>
                </div>
            </div>

            <div class="details-body">
                <div class="info-section">
                    <h3>Thông tin chung</h3>
                    <div class="info-grid">
                        <div class="info-item">
                            <label>Trạng thái:</label>
                            <span class="status-badge ${station.status === 'ACTIVE' ? 'status-active' : 'status-inactive'}">
                                ${station.status === 'ACTIVE' ? 'Hoạt động' : 'Bảo trì'}
                            </span>
                        </div>
                        <div class="info-item">
                            <label>Vĩ độ:</label>
                            <span>${station.latitude || 'N/A'}</span>
                        </div>
                        <div class="info-item">
                            <label>Kinh độ:</label>
                            <span>${station.longitude || 'N/A'}</span>
                        </div>
                        <div class="info-item">
                            <label>Điện thoại:</label>
                            <span>${station.phone || 'N/A'}</span>
                        </div>
                    </div>
                </div>

                <div class="chargers-section">
                    <div class="section-header">
                        <h3>Chargers (${station.chargers?.length || 0})</h3>
                        <button class="btn btn-sm btn-primary" onclick="stationsManagement.showCreateChargerModal(${station.id})">
                            <i class="fas fa-plus"></i> Thêm Charger
                        </button>
                    </div>
                    
                    <div class="chargers-grid">
                        ${renderChargers(station.chargers || [])}
                    </div>
                </div>
            </div>
        </div>
    `;
}

/**
 * Render chargers grid
 */
function renderChargers(chargers) {
    if (chargers.length === 0) {
        return '<div class="empty-state"><i class="fas fa-plug"></i><p>Chưa có charger nào</p></div>';
    }

    return chargers.map(charger => `
        <div class="charger-card">
            <div class="charger-header">
                <span class="charger-name">${charger.name || `Charger #${charger.id}`}</span>
                <span class="status-badge ${charger.status === 'AVAILABLE' ? 'status-active' : 'status-inactive'}">
                    ${getChargerStatusText(charger.status)}
                </span>
            </div>
            <div class="charger-body">
                <div class="charger-info">
                    <div><strong>Loại:</strong> ${charger.connectorType}</div>
                    <div><strong>Công suất:</strong> ${charger.powerOutput} kW</div>
                    <div><strong>Giá:</strong> ${formatCurrency(charger.pricePerKwh)}/kWh</div>
                </div>
                <div class="charger-actions">
                    <button class="btn btn-sm btn-outline" onclick="stationsManagement.showEditChargerModal(${charger.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="stationsManagement.deleteCharger(${charger.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * Show create station modal
 */
export function showCreateStationModal() {
    const modal = createModal('Thêm trạm sạc mới', `
        <form id="stationForm">
            <div class="form-group">
                <label>Tên trạm *</label>
                <input type="text" name="name" required />
            </div>
            <div class="form-group">
                <label>Địa chỉ *</label>
                <input type="text" name="address" required />
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Vĩ độ *</label>
                    <input type="number" name="latitude" step="any" required />
                </div>
                <div class="form-group">
                    <label>Kinh độ *</label>
                    <input type="number" name="longitude" step="any" required />
                </div>
            </div>
            <div class="form-group">
                <label>Điện thoại</label>
                <input type="tel" name="phone" />
            </div>
            <div class="form-group">
                <label>Trạng thái</label>
                <select name="status">
                    <option value="ACTIVE">Hoạt động</option>
                    <option value="MAINTENANCE">Bảo trì</option>
                </select>
            </div>
        </form>
    `, async () => {
        const form = document.getElementById('stationForm');
        const formData = new FormData(form);
        const data = Object.fromEntries(formData);
        
        await createStation(data);
        closeModal(modal);
    });
}

/**
 * Show edit station modal
 */
export async function showEditStationModal(stationId) {
    if (!selectedStation || selectedStation.id !== stationId) {
        await selectStation(stationId);
    }

    const station = selectedStation;
    
    const modal = createModal('Chỉnh sửa trạm sạc', `
        <form id="stationForm">
            <div class="form-group">
                <label>Tên trạm *</label>
                <input type="text" name="name" value="${station.name}" required />
            </div>
            <div class="form-group">
                <label>Địa chỉ *</label>
                <input type="text" name="address" value="${station.address || ''}" required />
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Vĩ độ *</label>
                    <input type="number" name="latitude" value="${station.latitude || ''}" step="any" required />
                </div>
                <div class="form-group">
                    <label>Kinh độ *</label>
                    <input type="number" name="longitude" value="${station.longitude || ''}" step="any" required />
                </div>
            </div>
            <div class="form-group">
                <label>Điện thoại</label>
                <input type="tel" name="phone" value="${station.phone || ''}" />
            </div>
            <div class="form-group">
                <label>Trạng thái</label>
                <select name="status">
                    <option value="ACTIVE" ${station.status === 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                    <option value="MAINTENANCE" ${station.status === 'MAINTENANCE' ? 'selected' : ''}>Bảo trì</option>
                </select>
            </div>
        </form>
    `, async () => {
        const form = document.getElementById('stationForm');
        const formData = new FormData(form);
        const data = Object.fromEntries(formData);
        
        await updateStation(stationId, data);
        closeModal(modal);
    });
}

/**
 * Create new station
 */
async function createStation(data) {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/admin/stations`, {
            method: 'POST',
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showNotification('Tạo trạm sạc thành công!', 'success');
            await loadStations();
        } else {
            const error = await response.json();
            showNotification(error.message || 'Không thể tạo trạm sạc', 'error');
        }
    } catch (error) {
        console.error('Error creating station:', error);
        showNotification('Lỗi kết nối đến máy chủ', 'error');
    }
}

/**
 * Update station
 */
async function updateStation(stationId, data) {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/admin/stations/${stationId}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showNotification('Cập nhật trạm sạc thành công!', 'success');
            await loadStations();
            await selectStation(stationId);
        } else {
            const error = await response.json();
            showNotification(error.message || 'Không thể cập nhật trạm sạc', 'error');
        }
    } catch (error) {
        console.error('Error updating station:', error);
        showNotification('Lỗi kết nối đến máy chủ', 'error');
    }
}

/**
 * Delete station
 */
export async function deleteStation(stationId) {
    if (!confirm('Bạn có chắc chắn muốn xóa trạm sạc này? Tất cả chargers sẽ bị xóa theo.')) {
        return;
    }

    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/admin/stations/${stationId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showNotification('Xóa trạm sạc thành công!', 'success');
            selectedStation = null;
            document.getElementById('stationDetails').innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-charging-station"></i>
                    <p>Chọn một trạm để xem chi tiết</p>
                </div>
            `;
            await loadStations();
        } else {
            const error = await response.json();
            showNotification(error.message || 'Không thể xóa trạm sạc', 'error');
        }
    } catch (error) {
        console.error('Error deleting station:', error);
        showNotification('Lỗi kết nối đến máy chủ', 'error');
    }
}

/**
 * Show create charger modal
 */
export function showCreateChargerModal(stationId) {
    const modal = createModal('Thêm Charger mới', `
        <form id="chargerForm">
            <div class="form-group">
                <label>Tên Charger</label>
                <input type="text" name="name" placeholder="Ví dụ: Charger A1" />
            </div>
            <div class="form-group">
                <label>Loại kết nối *</label>
                <select name="connectorType" required>
                    <option value="CCS2">CCS2</option>
                    <option value="CHAdeMO">CHAdeMO</option>
                    <option value="Type2">Type 2</option>
                    <option value="Type1">Type 1</option>
                </select>
            </div>
            <div class="form-group">
                <label>Công suất (kW) *</label>
                <input type="number" name="powerOutput" step="0.1" required />
            </div>
            <div class="form-group">
                <label>Giá (VND/kWh) *</label>
                <input type="number" name="pricePerKwh" step="100" required />
            </div>
            <div class="form-group">
                <label>Trạng thái</label>
                <select name="status">
                    <option value="AVAILABLE">Khả dụng</option>
                    <option value="OCCUPIED">Đang sử dụng</option>
                    <option value="MAINTENANCE">Bảo trì</option>
                    <option value="OFFLINE">Offline</option>
                </select>
            </div>
        </form>
    `, async () => {
        const form = document.getElementById('chargerForm');
        const formData = new FormData(form);
        const data = Object.fromEntries(formData);
        data.stationId = stationId;
        
        await createCharger(data);
        closeModal(modal);
    });
}

/**
 * Show edit charger modal
 */
export async function showEditChargerModal(chargerId) {
    // Find charger in selectedStation
    const charger = selectedStation?.chargers?.find(c => c.id === chargerId);
    if (!charger) return;

    const modal = createModal('Chỉnh sửa Charger', `
        <form id="chargerForm">
            <div class="form-group">
                <label>Tên Charger</label>
                <input type="text" name="name" value="${charger.name || ''}" />
            </div>
            <div class="form-group">
                <label>Loại kết nối *</label>
                <select name="connectorType" required>
                    <option value="CCS2" ${charger.connectorType === 'CCS2' ? 'selected' : ''}>CCS2</option>
                    <option value="CHAdeMO" ${charger.connectorType === 'CHAdeMO' ? 'selected' : ''}>CHAdeMO</option>
                    <option value="Type2" ${charger.connectorType === 'Type2' ? 'selected' : ''}>Type 2</option>
                    <option value="Type1" ${charger.connectorType === 'Type1' ? 'selected' : ''}>Type 1</option>
                </select>
            </div>
            <div class="form-group">
                <label>Công suất (kW) *</label>
                <input type="number" name="powerOutput" value="${charger.powerOutput}" step="0.1" required />
            </div>
            <div class="form-group">
                <label>Giá (VND/kWh) *</label>
                <input type="number" name="pricePerKwh" value="${charger.pricePerKwh}" step="100" required />
            </div>
            <div class="form-group">
                <label>Trạng thái</label>
                <select name="status">
                    <option value="AVAILABLE" ${charger.status === 'AVAILABLE' ? 'selected' : ''}>Khả dụng</option>
                    <option value="OCCUPIED" ${charger.status === 'OCCUPIED' ? 'selected' : ''}>Đang sử dụng</option>
                    <option value="MAINTENANCE" ${charger.status === 'MAINTENANCE' ? 'selected' : ''}>Bảo trì</option>
                    <option value="OFFLINE" ${charger.status === 'OFFLINE' ? 'selected' : ''}>Offline</option>
                </select>
            </div>
        </form>
    `, async () => {
        const form = document.getElementById('chargerForm');
        const formData = new FormData(form);
        const data = Object.fromEntries(formData);
        
        await updateCharger(chargerId, data);
        closeModal(modal);
    });
}

/**
 * Create new charger
 */
async function createCharger(data) {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/admin/chargers`, {
            method: 'POST',
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showNotification('Tạo Charger thành công!', 'success');
            await selectStation(data.stationId);
        } else {
            const error = await response.json();
            showNotification(error.message || 'Không thể tạo Charger', 'error');
        }
    } catch (error) {
        console.error('Error creating charger:', error);
        showNotification('Lỗi kết nối đến máy chủ', 'error');
    }
}

/**
 * Update charger
 */
async function updateCharger(chargerId, data) {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/admin/chargers/${chargerId}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showNotification('Cập nhật Charger thành công!', 'success');
            await selectStation(selectedStation.id);
        } else {
            const error = await response.json();
            showNotification(error.message || 'Không thể cập nhật Charger', 'error');
        }
    } catch (error) {
        console.error('Error updating charger:', error);
        showNotification('Lỗi kết nối đến máy chủ', 'error');
    }
}

/**
 * Delete charger
 */
export async function deleteCharger(chargerId) {
    if (!confirm('Bạn có chắc chắn muốn xóa Charger này?')) {
        return;
    }

    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/admin/chargers/${chargerId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showNotification('Xóa Charger thành công!', 'success');
            await selectStation(selectedStation.id);
        } else {
            const error = await response.json();
            showNotification(error.message || 'Không thể xóa Charger', 'error');
        }
    } catch (error) {
        console.error('Error deleting charger:', error);
        showNotification('Lỗi kết nối đến máy chủ', 'error');
    }
}

// Utility functions
function createModal(title, content, onSubmit) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>${title}</h3>
                <button class="btn-close" onclick="this.closest('.modal-overlay').remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="modal-body">
                ${content}
            </div>
            <div class="modal-footer">
                <button class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">
                    Hủy
                </button>
                <button class="btn btn-primary" id="modalSubmitBtn">
                    Xác nhận
                </button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    const submitBtn = modal.querySelector('#modalSubmitBtn');
    submitBtn.addEventListener('click', async () => {
        const form = modal.querySelector('form');
        if (form && !form.checkValidity()) {
            form.reportValidity();
            return;
        }
        await onSubmit();
    });

    return modal;
}

function closeModal(modal) {
    if (modal && modal.remove) {
        modal.remove();
    }
}

function getChargerStatusText(status) {
    const map = {
        'AVAILABLE': 'Khả dụng',
        'OCCUPIED': 'Đang dùng',
        'MAINTENANCE': 'Bảo trì',
        'OFFLINE': 'Offline'
    };
    return map[status] || status;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Export for global access
window.stationsManagement = {
    initStationsManagement,
    selectStation,
    showCreateStationModal,
    showEditStationModal,
    deleteStation,
    showCreateChargerModal,
    showEditChargerModal,
    deleteCharger
};
