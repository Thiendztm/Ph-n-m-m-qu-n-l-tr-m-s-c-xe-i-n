import { state, initializeData } from './data.js';
import { setupSidebar } from './navigation.js';
import { renderStationsPage } from './stations.js';
import { renderUsersPage } from './users.js';
import { renderAccountsPage } from './accounts.js';
import { renderReportsPage } from './reports.js';
import { renderRevenuePage } from './revenue.js';
import { renderSupportPage } from './support.js';

export function renderPage() {
  const mainContent = document.querySelector('.main-content');
  if (!mainContent) return;

  switch(state.currentPage) {
    case 'stations':
    case 'devices':
      renderStationsPage(); break;
    case 'users':
      renderUsersPage(); break;
    case 'accounts':
      renderAccountsPage(); break;
    case 'reports':
      renderReportsPage(); break;
    case 'revenue':
      renderRevenuePage(); break;
    case 'support':
      renderSupportPage(); break;
    case 'logout':
      handleLogout(); break;
  }
}

function handleLogout() {
  if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
    setTimeout(() => { window.location.href = './login.html'; }, 1000);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  initializeData();
  setupSidebar();
  renderPage();
});