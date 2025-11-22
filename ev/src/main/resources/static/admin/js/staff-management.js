// staff-management.js - Quản lý tài khoản Staff
import { api } from './api-client.js';
import { createModal, closeModal, showNotification } from './utils.js';

let staffList = [];

export function renderStaffManagementPage() {
  const main = document.querySelector('.main-content');
  main.innerHTML = `
    <div class="content-header">
      <h1>Quản lý Nhân viên</h1>
      <button class="btn primary" onclick="showAddStaffModal()">
        <i class="fa-solid fa-user-plus"></i> Thêm nhân viên
      </button>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Họ tên</th>
            <th>Email</th>
            <th>Số điện thoại</th>
            <th>Role</th>
            <th>Ngày tạo</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody id="staffTableBody">
          <tr><td colspan="7">Đang tải...</td></tr>
        </tbody>
      </table>
    </div>
  `;
  
  loadStaffList();
}

async function loadStaffList() {
  try {
    const response = await api.get('/admin/users?role=CS_STAFF');
    staffList = response.users || response;
    renderStaffTable();
  } catch (error) {
    console.error('Failed to load staff list:', error);
    const tbody = document.getElementById('staffTableBody');
    if (tbody) {
      tbody.innerHTML = `<tr><td colspan="7" style="color: red;">Lỗi: ${error.message}</td></tr>`;
    }
  }
}

function renderStaffTable() {
  const tbody = document.getElementById('staffTableBody');
  if (!tbody) return;
  
  if (staffList.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7">Không có nhân viên nào</td></tr>';
    return;
  }
  
  tbody.innerHTML = '';
  staffList.forEach(staff => {
    const fullName = (staff.firstName && staff.lastName) 
      ? `${staff.firstName} ${staff.lastName}` 
      : staff.email;
    const createdDate = staff.createdAt 
      ? new Date(staff.createdAt).toLocaleString('vi-VN') 
      : '--';
    
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${staff.id}</td>
      <td>${fullName}</td>
      <td>${staff.email}</td>
      <td>${staff.phone || '--'}</td>
      <td><span class="status-badge active">${staff.role}</span></td>
      <td>${createdDate}</td>
      <td>
        <button class="btn-icon" onclick="editStaff(${staff.id})" title="Sửa">
          <i class="fa-solid fa-edit"></i>
        </button>
        <button class="btn-icon danger" onclick="deleteStaff(${staff.id})" title="Xóa">
          <i class="fa-solid fa-trash"></i>
        </button>
      </td>
    `;
    tbody.appendChild(row);
  });
}

// Show modal to add new staff
window.showAddStaffModal = function() {
  const modal = createModal('Thêm tài khoản nhân viên', `
    <form id="addStaffForm">
      <div class="form-group">
        <label>Email *</label>
        <input type="email" name="email" required placeholder="staff@example.com">
      </div>
      <div class="form-group">
        <label>Mật khẩu *</label>
        <input type="password" name="password" required placeholder="Ít nhất 8 ký tự" minlength="8">
      </div>
      <div class="form-group">
        <label>Họ và tên *</label>
        <input type="text" name="fullName" required placeholder="Nguyễn Văn A">
      </div>
      <div class="form-group">
        <label>Số điện thoại</label>
        <input type="tel" name="phoneNumber" placeholder="0123456789">
      </div>
      <div class="form-note" style="color: #666; font-size: 14px; margin-top: 10px;">
        * Trường bắt buộc
      </div>
    </form>
  `, [
    { text: 'Hủy', class: 'btn', onclick: 'closeModal()' },
    { text: 'Tạo tài khoản', class: 'btn primary', onclick: 'createStaffAccount()' }
  ]);
  document.body.appendChild(modal);
};

// Create staff account
window.createStaffAccount = async function() {
  const form = document.getElementById('addStaffForm');
  const formData = new FormData(form);
  
  const staffData = {
    email: formData.get('email'),
    password: formData.get('password'),
    fullName: formData.get('fullName'),
    phoneNumber: formData.get('phoneNumber') || ''
  };
  
  // Validate
  if (!staffData.email || !staffData.password || !staffData.fullName) {
    showNotification('Vui lòng điền đầy đủ thông tin bắt buộc!', 'error');
    return;
  }
  
  if (staffData.password.length < 8) {
    showNotification('Mật khẩu phải có ít nhất 8 ký tự!', 'error');
    return;
  }
  
  try {
    const response = await api.post('/admin/staff', staffData);
    
    if (response.success) {
      showNotification('Tạo tài khoản nhân viên thành công!', 'success');
      closeModal();
      loadStaffList(); // Reload table
    } else {
      showNotification(response.error || 'Không thể tạo tài khoản', 'error');
    }
  } catch (error) {
    console.error('Failed to create staff:', error);
    showNotification('Lỗi: ' + error.message, 'error');
  }
};

// Edit staff
window.editStaff = function(id) {
  const staff = staffList.find(s => s.id === id);
  if (!staff) return;
  
  const fullName = (staff.firstName && staff.lastName) 
    ? `${staff.firstName} ${staff.lastName}` 
    : '';
  
  const modal = createModal('Chỉnh sửa nhân viên', `
    <form id="editStaffForm">
      <input type="hidden" name="id" value="${staff.id}">
      <div class="form-group">
        <label>Email</label>
        <input type="email" name="email" value="${staff.email}" disabled>
        <small style="color: #666;">Email không thể thay đổi</small>
      </div>
      <div class="form-group">
        <label>Họ và tên</label>
        <input type="text" name="fullName" value="${fullName}" required>
      </div>
      <div class="form-group">
        <label>Số điện thoại</label>
        <input type="tel" name="phone" value="${staff.phone || ''}">
      </div>
    </form>
  `, [
    { text: 'Hủy', class: 'btn', onclick: 'closeModal()' },
    { text: 'Cập nhật', class: 'btn primary', onclick: `updateStaff(${id})` }
  ]);
  document.body.appendChild(modal);
};

// Update staff
window.updateStaff = async function(id) {
  const form = document.getElementById('editStaffForm');
  const formData = new FormData(form);
  
  const updates = {
    fullName: formData.get('fullName'),
    phone: formData.get('phone')
  };
  
  try {
    // Note: Backend may not have this endpoint yet
    await api.put(`/admin/users/${id}`, updates);
    showNotification('Cập nhật thành công!', 'success');
    closeModal();
    loadStaffList();
  } catch (error) {
    console.error('Failed to update staff:', error);
    showNotification('Lỗi: ' + error.message, 'error');
  }
};

// Delete staff
window.deleteStaff = async function(id) {
  const staff = staffList.find(s => s.id === id);
  if (!staff) return;
  
  const fullName = (staff.firstName && staff.lastName) 
    ? `${staff.firstName} ${staff.lastName}` 
    : staff.email;
  
  if (!confirm(`Bạn có chắc chắn muốn xóa nhân viên "${fullName}"?`)) {
    return;
  }
  
  try {
    await api.delete(`/admin/users/${id}`);
    showNotification('Đã xóa nhân viên!', 'success');
    loadStaffList();
  } catch (error) {
    console.error('Failed to delete staff:', error);
    showNotification('Lỗi: ' + error.message, 'error');
  }
};
