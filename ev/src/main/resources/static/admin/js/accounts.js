// js/accounts.js
import { state, fetchAccounts } from './data.js';
import { createModal, closeModal, showNotification } from './utils.js';
import { api } from './api-client.js';

export function renderAccountsPage() {
  const main = document.querySelector('.main-content');
  main.innerHTML = `
    <div class="content-header">
      <h1>Quản lý Tài khoản</h1>
    </div>
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th><th>Người dùng</th><th>Số dư</th><th>Trạng thái</th><th>Giao dịch cuối</th><th>Hành động</th>
          </tr>
        </thead>
        <tbody id="accountsTableBody"></tbody>
      </table>
    </div>
  `;
  renderAccountsTable();
}

export function renderAccountsTable() {
  const tbody = document.getElementById('accountsTableBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="6">Đang tải...</td></tr>';
  
  // Fetch fresh data from API
  fetchAccounts().then(() => {
    tbody.innerHTML = '';
    if (state.accounts.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6">Không có tài khoản nào</td></tr>';
      return;
    }
    state.accounts.forEach(a => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${a.id}</td>
        <td>${a.userName}</td>
        <td>${a.balance.toLocaleString()}đ</td>
        <td><span class="status-badge ${a.status}">${a.status === 'active' ? 'Hoạt động' : 'Đóng băng'}</span></td>
        <td>${a.lastTransaction}</td>
        <td>
          <button class="btn-icon" onclick="viewAccountDetails('${a.id}')"><i class="fa-solid fa-eye"></i></button>
          <button class="btn-icon" onclick="toggleAccountStatus('${a.id}')"><i class="fa-solid fa-toggle-${a.status === 'active' ? 'on' : 'off'}"></i></button>
        </td>
      `;
      tbody.appendChild(row);
    });
  }).catch(error => {
    tbody.innerHTML = `<tr><td colspan="6" style="color: red;">Lỗi: ${error.message}</td></tr>`;
  });
}

window.viewAccountDetails = viewAccountDetails;
function viewAccountDetails(id) {
  const a = state.accounts.find(x => x.id === id);
  if (!a) return;
  const modal = createModal(`Chi tiết ${a.id}`, `
    <p><strong>Người dùng:</strong> ${a.userName}</p>
    <p><strong>Số dư:</strong> ${a.balance.toLocaleString()}đ</p>
    <p><strong>Trạng thái:</strong> ${a.status === 'active' ? 'Hoạt động' : 'Đóng băng'}</p>
    <p><strong>Giao dịch cuối:</strong> ${a.lastTransaction}</p>
  `, [{ text: 'Đóng', class: 'btn primary', onclick: 'closeModal()' }]);
  document.body.appendChild(modal);
}

window.toggleAccountStatus = toggleAccountStatus;
function toggleAccountStatus(id) {
  const a = state.accounts.find(x => x.id === id);
  if (a) {
    a.status = a.status === 'active' ? 'frozen' : 'active';
    renderAccountsTable();
    showNotification(`Đã ${a.status === 'active' ? 'kích hoạt' : 'đóng băng'} tài khoản`, 'success');
  }
}