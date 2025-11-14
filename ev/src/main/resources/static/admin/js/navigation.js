import { state } from './data.js';
import { renderPage } from './main.js';

export function navigateTo(page) {
  state.currentPage = page;
  updateSidebarActive(page);
  renderPage();
}

export function updateSidebarActive(page) {
  const menuItems = document.querySelectorAll('.sidebar-menu li');
  menuItems.forEach(item => item.classList.remove('active'));
  const pageMap = { 'stations': 0, 'devices': 1, 'users': 2, 'accounts': 3, 'reports': 4, 'revenue': 5, 'support': 6, 'logout': 7 };
  if (pageMap[page] !== undefined) {
    menuItems[pageMap[page]].classList.add('active');
  }
}

export function setupSidebar() {
  const items = document.querySelectorAll('.sidebar-menu li');
  const pages = ['stations', 'devices', 'users', 'accounts', 'reports', 'revenue', 'support', 'logout'];
  items.forEach((li, index) => {
    li.addEventListener('click', () => {
      if (pages[index]) navigateTo(pages[index]);
    });
  });
}