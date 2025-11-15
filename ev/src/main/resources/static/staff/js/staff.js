/**
 * Main entry point for Staff Dashboard
 */

import { state } from './data.js';
import { loadStations } from './stations.js';
import { showNotification } from './utils.js';

// Check authentication
function checkAuth() {
  const token = localStorage.getItem('jwt_token');
  const userRole = localStorage.getItem('userRole');

  if (!token) {
    alert('Bạn cần đăng nhập để truy cập trang này');
    window.location.href = '../login.html';
    return false;
  }

  if (userRole !== 'CS_STAFF' && userRole !== 'ADMIN') {
    alert('Bạn không có quyền truy cập trang này');
    window.location.href = '../index.html';
    return false;
  }

  return true;
}

// Page navigation
function setupNavigation() {
  const menuItems = document.querySelectorAll('.menu li');
  const pageTitle = document.getElementById('pageTitle');
  const content = document.getElementById('content');

  menuItems.forEach(item => {
    item.addEventListener('click', () => {
      const page = item.dataset.page;
      
      // Update active menu
      menuItems.forEach(mi => mi.classList.remove('active'));
      item.classList.add('active');
      
      // Update page title and content
      state.currentPage = page;
      
      switch(page) {
        case 'monitoring':
          pageTitle.textContent = 'Giám sát điểm sạc';
          renderMonitoringPage();
          break;
        case 'payments':
          pageTitle.textContent = 'Quản lý thanh toán';
          renderPaymentsPage();
          break;
        case 'incidents':
          pageTitle.textContent = 'Báo cáo sự cố';
          renderIncidentsPage();
          break;
      }
    });
  });
}

function renderMonitoringPage() {
  const content = document.getElementById('content');
  
  content.innerHTML = `
    <div class="page-header">
      <div class="station-selector">
        <label for="stationSelect">Chọn trạm sạc:</label>
        <select id="stationSelect"></select>
      </div>
    </div>
    <div id="monitoringContent" class="monitoring-container">
      <p class="text-muted">Vui lòng chọn trạm sạc để xem thông tin</p>
    </div>
  `;

  // Load stations
  loadStations();
}

function renderPaymentsPage() {
  const content = document.getElementById('content');
  
  content.innerHTML = `
    <div class="payments-container">
      <div class="payment-form card">
        <h3>Thanh toán tiền mặt</h3>
        <form id="cashPaymentForm">
          <div class="form-group">
            <label>Mã phiên sạc:</label>
            <input type="number" id="sessionId" required />
          </div>
          <div class="form-group">
            <label>Số tiền:</label>
            <input type="number" id="amount" required />
          </div>
          <div class="form-group">
            <label>Ghi chú:</label>
            <textarea id="notes" rows="3"></textarea>
          </div>
          <button type="submit" class="btn btn-primary">Xác nhận thanh toán</button>
        </form>
      </div>

      <div class="recent-payments card">
        <h3>Thanh toán gần đây</h3>
        <div id="recentPaymentsList">
          <p class="text-muted">Chưa có thanh toán nào</p>
        </div>
      </div>
    </div>
  `;

  // Setup payment form handler
  const form = document.getElementById('cashPaymentForm');
  form.addEventListener('submit', handleCashPayment);
}

function renderIncidentsPage() {
  const content = document.getElementById('content');
  
  content.innerHTML = `
    <div class="incidents-container">
      <div class="incident-form card">
        <h3>Báo cáo sự cố mới</h3>
        <form id="incidentForm">
          <div class="form-group">
            <label>Trạm sạc:</label>
            <select id="incidentStation" required></select>
          </div>
          <div class="form-group">
            <label>Điểm sạc:</label>
            <select id="incidentCharger" required></select>
          </div>
          <div class="form-group">
            <label>Loại sự cố:</label>
            <select id="incidentType" required>
              <option value="HARDWARE">Lỗi phần cứng</option>
              <option value="SOFTWARE">Lỗi phần mềm</option>
              <option value="CONNECTIVITY">Lỗi kết nối</option>
              <option value="OTHER">Khác</option>
            </select>
          </div>
          <div class="form-group">
            <label>Mô tả:</label>
            <textarea id="incidentDescription" rows="4" required></textarea>
          </div>
          <button type="submit" class="btn btn-danger">Gửi báo cáo</button>
        </form>
      </div>

      <div class="incidents-list card">
        <h3>Sự cố đã báo cáo</h3>
        <div id="incidentsList">
          <p class="text-muted">Chưa có sự cố nào</p>
        </div>
      </div>
    </div>
  `;

  // Load station select for incidents
  loadStations();
  
  // Setup incident form handler
  const form = document.getElementById('incidentForm');
  form.addEventListener('submit', handleIncidentReport);
}

async function handleCashPayment(e) {
  e.preventDefault();
  
  const sessionId = document.getElementById('sessionId').value;
  const amount = document.getElementById('amount').value;
  const notes = document.getElementById('notes').value;

  // TODO: Implement API call
  showNotification('Chức năng thanh toán đang được phát triển', 'warning');
}

async function handleIncidentReport(e) {
  e.preventDefault();
  
  const stationId = document.getElementById('incidentStation').value;
  const chargerId = document.getElementById('incidentCharger').value;
  const type = document.getElementById('incidentType').value;
  const description = document.getElementById('incidentDescription').value;

  // TODO: Implement API call
  showNotification('Chức năng báo cáo sự cố đang được phát triển', 'warning');
}

// Refresh button handler
document.getElementById('refreshBtn')?.addEventListener('click', () => {
  switch(state.currentPage) {
    case 'monitoring':
      if (state.currentStation) {
        loadStations();
      }
      break;
    default:
      location.reload();
  }
});

// Initialize
document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuth()) return;
  
  setupNavigation();
  renderMonitoringPage(); // Default page
  
  // Display staff name
  const userName = localStorage.getItem('userName') || 'Staff';
  document.querySelector('.brand span').textContent = `Staff: ${userName}`;
});
