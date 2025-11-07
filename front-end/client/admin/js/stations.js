import { state } from './data.js';
import { showNotification, createModal, closeModal } from './utils.js';

export function renderStationsPage() {
  const mainContent = document.querySelector('.main-content');
  mainContent.innerHTML = `
    <div class="content-header">
      <h1>Quản lý Trạm sạc</h1>
      <button class="btn primary" onclick="showAddStationModal()">
        <i class="fa-solid fa-plus"></i> Thêm trạm sạc
      </button>
    </div>
    <div class="stations-grid" id="stationsGrid"></div>
  `;
  renderStations();
}

function createStationCard(station) {
  const card = document.createElement('div');
  card.className = 'station-card';
  card.innerHTML = `
    <div class="card-header">
      <i class="fa-solid fa-bolt"></i> ${station.id}
      <div class="card-header-actions">
        <button class="btn-icon" onclick="editStation('${station.id}')" title="Chỉnh sửa"><i class="fa-solid fa-edit"></i></button>
        <button class="btn-icon danger" onclick="deleteStation('${station.id}')" title="Xóa"><i class="fa-solid fa-trash"></i></button>
      </div>
    </div>
    <div class="card-body">
      <div class="station-info">
        <h3>${station.name}</h3>
        <p class="station-address">${station.address}</p>
        <div class="station-details">
          <span class="chip">${station.connector}</span>
          <span class="chip">${station.power}kW</span>
          <span class="chip">${station.price.toLocaleString()}đ/kWh</span>
        </div>
      </div>
      <div class="status ${station.status}">
        <span class="status-text">${station.status === 'available' ? 'Sẵn sàng' : 'Đang sử dụng'}</span>
        <span class="status-badge">${station.status.toUpperCase()}</span>
      </div>
      <div class="info-row"><span>${station.kwh} kWh</span><span>${station.temp} °C</span></div>
      <div class="info-row"><span>${station.kw} kW</span><span>${station.amp} A</span></div>
      <div class="info-row"><span>${station.soc} %</span><span>${station.volt} V</span></div>
      <div class="card-actions">
        <button class="btn primary" onclick="startCharging('${station.id}')">Bắt đầu</button>
        <button class="btn" onclick="viewStationDetails('${station.id}')">Chi tiết</button>
        <button class="btn danger" onclick="stopCharging('${station.id}')">Dừng</button>
      </div>
    </div>
  `;
  return card;
}

export function renderStations() {
  const grid = document.getElementById('stationsGrid');
  if (!grid) return;
  grid.innerHTML = '';
  state.stations.forEach(s => grid.appendChild(createStationCard(s)));  // ĐÃ SỬA
}

// CRUD Functions
window.showAddStationModal = showAddStationModal;
function showAddStationModal() {
  const modal = createModal('Thêm trạm sạc mới', `
    <form id="stationForm">
      <div class="form-group"><label>Tên trạm sạc</label><input type="text" name="name" required></div>
      <div class="form-row">
        <div class="form-group"><label>Vĩ độ</label><input type="number" name="lat" step="0.0001" required></div>
        <div class="form-group"><label>Kinh độ</label><input type="number" name="lng" step="0.0001" required></div>
      </div>
      <div class="form-group">
        <label>Loại cổng sạc</label>
        <select name="connector" required>
          <option value="CCS">CCS</option>
          <option value="AC">AC</option>
          <option value="CHAdeMO">CHAdeMO</option>
        </select>
      </div>
      <div class="form-group"><label>Công suất (kW)</label><input type="number" name="power" required></div>
      <div class="form-group"><label>Đơn giá (đ/kWh)</label><input type="number" name="price" required></div>
      <div class="form-group"><label>Địa chỉ</label><textarea name="address" required></textarea></div>
    </form>
  `, [
    { text: 'Hủy', class: 'btn', onclick: 'closeModal()' },
    { text: 'Thêm', class: 'btn primary', onclick: 'addStation()' }
  ]);
  document.body.appendChild(modal);
}

window.addStation = addStation;
function addStation() {
  const form = document.getElementById('stationForm');
  const data = new FormData(form);
  const newStation = {
    id: `FC-${Date.now()}`,
    name: data.get('name'),
    lat: parseFloat(data.get('lat')),
    lng: parseFloat(data.get('lng')),
    connector: data.get('connector'),
    status: 'available',
    power: parseInt(data.get('power')),
    price: parseInt(data.get('price')),
    address: data.get('address'),
    distance: '0km', kwh: '0', temp: '25', kw: '0', amp: '0', soc: '0', volt: '0'
  };
  state.stations.push(newStation);  // ĐÃ SỬA
  renderStations();
  closeModal();
  showNotification('Đã thêm trạm sạc mới thành công!', 'success');
}

window.editStation = editStation;
function editStation(id) {
  const s = state.stations.find(x => x.id === id);  // ĐÃ SỬA
  if (!s) return;
  const modal = createModal('Chỉnh sửa trạm sạc', `
    <form id="editForm">
      <input type="hidden" name="id" value="${s.id}">
      <div class="form-group"><label>Tên</label><input type="text" name="name" value="${s.name}" required></div>
      <div class="form-group"><label>Địa chỉ</label><textarea name="address" required>${s.address}</textarea></div>
      <div class="form-group"><label>Công suất (kW)</label><input type="number" name="power" value="${s.power}" required></div>
      <div class="form-group"><label>Giá (đ/kWh)</label><input type="number" name="price" value="${s.price}" required></div>
    </form>
  `, [
    { text: 'Hủy', class: 'btn', onclick: 'closeModal()' },
    { text: 'Cập nhật', class: 'btn primary', onclick: `updateStation('${id}')` }
  ]);
  document.body.appendChild(modal);
}

window.updateStation = updateStation;
function updateStation(id) {
  const form = document.getElementById('editForm');
  const data = new FormData(form);
  const idx = state.stations.findIndex(x => x.id === id);  // ĐÃ SỬA
  if (idx !== -1) {
    state.stations[idx] = {
      ...state.stations[idx],
      name: data.get('name'),
      address: data.get('address'),
      power: +data.get('power'),
      price: +data.get('price')
    };
    renderStations();
    closeModal();
    showNotification('Cập nhật thành công!', 'success');
  }
}

window.deleteStation = deleteStation;
function deleteStation(id) {
  if (confirm('Xóa trạm này?')) {
    state.stations = state.stations.filter(x => x.id !== id);  // ĐÃ SỬA
    renderStations();
    showNotification('Đã xóa!', 'success');
  }
}

window.startCharging = startCharging;
function startCharging(id) {
  const s = state.stations.find(x => x.id === id);  // ĐÃ SỬA
  if (s && s.status === 'available') {
    s.status = 'busy';
    renderStations();
    showNotification('Đã bắt đầu sạc!', 'success');
  }
}

window.stopCharging = stopCharging;
function stopCharging(id) {
  const s = state.stations.find(x => x.id === id);  // ĐÃ SỬA
  if (s && s.status === 'busy') {
    s.status = 'available';
    renderStations();
    showNotification('Đã dừng sạc!', 'success');
  }
}

window.viewStationDetails = viewStationDetails;
function viewStationDetails(id) {
  const s = state.stations.find(x => x.id === id);  // ĐÃ SỬA
  if (!s) return;
  const modal = createModal(`Chi tiết ${s.name}`, `
    <p><strong>ID:</strong> ${s.id}</p>
    <p><strong>Địa chỉ:</strong> ${s.address}</p>
    <p><strong>Cổng:</strong> ${s.connector}</p>
    <p><strong>Công suất:</strong> ${s.power} kW</p>
    <p><strong>Giá:</strong> ${s.price.toLocaleString()}đ/kWh</p>
    <p><strong>Trạng thái:</strong> ${s.status === 'available' ? 'Sẵn sàng' : 'Đang dùng'}</p>
  `, [{ text: 'Đóng', class: 'btn primary', onclick: 'closeModal()' }]);
  document.body.appendChild(modal);
}