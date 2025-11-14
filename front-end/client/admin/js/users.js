import { state } from './data.js';
import { createModal, closeModal, showNotification } from './utils.js';

export function renderUsersPage() {
  const main = document.querySelector('.main-content');
  main.innerHTML = `
    <div class="content-header">
      <h1>Quản lý Người dùng</h1>
      <button class="btn primary" onclick="showAddUserModal()"><i class="fa-solid fa-plus"></i> Thêm người dùng</button>
    </div>
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th><th>Tên</th><th>Email</th>
            <th>SĐT</th>
            <th>Loại xe</th>
            <th>Biển số</th>
            <th>Trạng thái</th><th>Ngày tham gia</th><th>Lần sạc</th><th>Tổng chi</th><th>Hành động</th>
          </tr>
        </thead>
        <tbody id="usersTableBody"></tbody>
      </table>
    </div>
  `;
  renderUsersTable();
}

export function renderUsersTable() {
  const tbody = document.getElementById('usersTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';
  state.users.forEach(u => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${u.id}</td>
      <td>${u.name}</td>
      <td>${u.email}</td>
      <td>${u.phone}</td>
      <td>${u.vehicleType || '--'}</td>
      <td>${u.vehicleId || '--'}</td>
      <td><span class="status-badge ${u.status}">${u.status === 'active' ? 'Hoạt động' : 'Vô hiệu'}</span></td>
      <td>${u.joinDate}</td>
      <td>${u.totalCharges}</td>
      <td>${u.totalSpent.toLocaleString()}đ</td>
      <td>
        <button class="btn-icon" onclick="editUser('${u.id}')"><i class="fa-solid fa-edit"></i></button>
        <button class="btn-icon danger" onclick="deleteUser('${u.id}')"><i class="fa-solid fa-trash"></i></button>
        <button class="btn-icon" onclick="toggleUserStatus('${u.id}')"><i class="fa-solid fa-toggle-${u.status === 'active' ? 'on' : 'off'}"></i></button>
      </td>
    `;
    tbody.appendChild(row);
  });
}

window.showAddUserModal = showAddUserModal;
function showAddUserModal() {
  const modal = createModal('Thêm người dùng', `
    <form id="userForm">
      <div class="form-group"><label>Tên</label><input type="text" name="name" required></div>
      <div class="form-group"><label>Email</label><input type="email" name="email" required></div>
      <div class="form-group"><label>SĐT</label><input type="tel" name="phone" required></div>
      <div class="form-group"><label>Biển số xe</label><input type="text" name="vehicleId" placeholder="51H-12345"></div>
      <div class="form-group"><label>Loại xe</label>
        <select name="vehicleType">
          <option value="">-- Chọn loại xe --</option>
          <option value="VinFast VF e34">VinFast VF e34</option>
          <option value="Tesla Model 3">Tesla Model 3</option>
          <option value="Hyundai Kona Electric">Hyundai Kona Electric</option>
          <option value="Kia EV6">Kia EV6</option>
          <option value="Mercedes EQS">Mercedes EQS</option>
          <option value="BMW i4">BMW i4</option>
        </select>
      </div>
    </form>
  `, [
    { text: 'Hủy', class: 'btn', onclick: 'closeModal()' },
    { text: 'Thêm', class: 'btn primary', onclick: 'addUser()' }
  ]);
  document.body.appendChild(modal);
}

window.addUser = addUser;
function addUser() {
  const form = document.getElementById('userForm');
  const d = new FormData(form);
  const newUser = {
    id: `U${String(state.users.length + 1).padStart(3, '0')}`,
    name: d.get('name'),
    email: d.get('email'),
    phone: d.get('phone'),
    vehicleId: d.get('vehicleId') || null,
    vehicleType: d.get('vehicleType') || null,
    status: 'active',
    joinDate: new Date().toISOString().split('T')[0],
    totalCharges: 0,
    totalSpent: 0
  };
  state.users.push(newUser);
  renderUsersTable();
  closeModal();
  showNotification('Thêm thành công!', 'success');
}

window.editUser = editUser;
function editUser(id) {
  const u = state.users.find(x => x.id === id);
  const modal = createModal('Chỉnh sửa', `
    <form id="editUserForm">
      <input type="hidden" name="id" value="${u.id}">
      <div class="form-group"><label>Tên</label><input type="text" name="name" value="${u.name}" required></div>
      <div class="form-group"><label>Email</label><input type="email" name="email" value="${u.email}" required></div>
      <div class="form-group"><label>SĐT</label><input type="tel" name="phone" value="${u.phone}" required></div>
    </form>
  `, [
    { text: 'Hủy', class: 'btn', onclick: 'closeModal()' },
    { text: 'Cập nhật', class: 'btn primary', onclick: `updateUser('${id}')` }
  ]);
  document.body.appendChild(modal);
}

window.updateUser = updateUser;
function updateUser(id) {
  const form = document.getElementById('editUserForm');
  const d = new FormData(form);
  const idx = state.users.findIndex(x => x.id === id);
  if (idx !== -1) {
    state.users[idx] = { ...state.users[idx], name: d.get('name'), email: d.get('email'), phone: d.get('phone') };
    renderUsersTable();
    closeModal();
    showNotification('Cập nhật thành công!', 'success');
  }
}

window.deleteUser = deleteUser;
function deleteUser(id) {
  if (confirm('Xóa người dùng này?')) {
    state.users = state.users.filter(x => x.id !== id);
    renderUsersTable();
    showNotification('Đã xóa!', 'success');
  }
}

window.toggleUserStatus = toggleUserStatus;
function toggleUserStatus(id) {
  const u = state.users.find(x => x.id === id);
  if (u) {
    u.status = u.status === 'active' ? 'inactive' : 'active';
    renderUsersTable();
    showNotification(`Đã ${u.status === 'active' ? 'kích hoạt' : 'vô hiệu hóa'}`, 'success');
  }
}