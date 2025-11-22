import { state, initializeData } from './data.js';
import { setupSidebar } from './navigation.js';
import { initStationsManagement } from './stations-management.js';
import { initUsersManagement } from './users-management.js';
import { initReports } from './reports.js';
import { renderStationsPage } from './stations.js';
import { renderUsersPage } from './users.js';
import { renderAccountsPage } from './accounts.js';
import { renderRevenuePage } from './revenue.js';
import { renderSupportPage } from './support.js';
import { renderStaffManagementPage } from './staff-management.js';

export function renderPage() {
  const mainContent = document.querySelector('.main-content');
  if (!mainContent) return;

  switch(state.currentPage) {
    case 'stations':
      initStationsManagement(); 
      break;
    case 'devices':
      renderStationsPage(); 
      break;
    case 'users':
      initUsersManagement(); 
      break;
    case 'staff':
      renderStaffManagementPage(); 
      break;
    case 'accounts':
      renderAccountsPage(); 
      break;
    case 'reports':
      initReports(); 
      break;
    case 'revenue':
      renderRevenuePage(); 
      break;
    case 'support':
      renderSupportPage(); 
      break;
    case 'logout':
      handleLogout(); 
      break;
  }
}

function handleLogout() {
  if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
    localStorage.clear();
    window.location.href = './login.html';
  }
}

// Check authentication
function checkAuth() {
  const token = localStorage.getItem('accessToken');
  const role = localStorage.getItem('userRole');
  
  if (!token) {
    // Hiển thị thông báo cần đăng nhập
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
      mainContent.innerHTML = `
        <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; text-align: center; padding: 40px;">
          <i class="fas fa-lock" style="font-size: 64px; color: #6366f1; margin-bottom: 20px;"></i>
          <h2 style="color: var(--text); margin-bottom: 10px; font-size: 24px;">Yêu cầu đăng nhập</h2>
          <p style="color: var(--muted); margin-bottom: 30px; max-width: 500px;">
            Bạn cần đăng nhập với tài khoản Admin để truy cập trang này.
          </p>
          <a href="./login.html" class="btn-primary" style="
            background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
            color: white;
            padding: 12px 32px;
            border-radius: 10px;
            text-decoration: none;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
            transition: all 0.3s ease;
          " onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 6px 20px rgba(79, 70, 229, 0.4)';" 
             onmouseout="this.style.transform='translateY(0)'; this.style.boxShadow='0 4px 12px rgba(79, 70, 229, 0.3)';">
            <i class="fas fa-sign-in-alt"></i> Đăng nhập ngay
          </a>
        </div>
      `;
    }
    return false;
  }
  
  if (role !== 'ADMIN') {
    // Hiển thị thông báo không có quyền
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
      mainContent.innerHTML = `
        <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; text-align: center; padding: 40px;">
          <i class="fas fa-ban" style="font-size: 64px; color: #ef4444; margin-bottom: 20px;"></i>
          <h2 style="color: var(--text); margin-bottom: 10px; font-size: 24px;">Không có quyền truy cập</h2>
          <p style="color: var(--muted); margin-bottom: 30px; max-width: 500px;">
            Bạn cần tài khoản Admin để truy cập trang này.<br>
            Role hiện tại: <strong>${role || 'Unknown'}</strong>
          </p>
          <button onclick="localStorage.clear(); window.location.href='./login.html';" class="btn-primary" style="
            background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
            color: white;
            padding: 12px 32px;
            border-radius: 10px;
            border: none;
            font-weight: 600;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
          ">
            <i class="fas fa-sign-out-alt"></i> Đăng xuất và đăng nhập lại
          </button>
        </div>
      `;
    }
    return false;
  }
  
  // Update admin name if available
  const userName = localStorage.getItem('userName');
  const adminNameEl = document.getElementById('adminName');
  if (userName && adminNameEl) {
    adminNameEl.textContent = `Welcome, ${userName}`;
  }
  
  return true;
}

document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuth()) return;
  
  initializeData();
  setupSidebar();
  renderPage();
});