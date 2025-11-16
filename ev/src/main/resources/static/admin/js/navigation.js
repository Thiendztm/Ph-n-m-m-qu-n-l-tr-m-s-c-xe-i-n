import { state } from './data.js';
import { renderPage } from './main.js';

export function navigateTo(page) {
  state.currentPage = page;
  updateSidebarActive(page);
  renderPage();
}

export function updateSidebarActive(page) {
  const menuItems = document.querySelectorAll('.sidebar-menu li');
  menuItems.forEach(item => {
    item.classList.remove('active');
    if (item.getAttribute('data-page') === page) {
      item.classList.add('active');
    }
  });
}

export function setupSidebar() {
  const items = document.querySelectorAll('.sidebar-menu li');
  items.forEach((li) => {
    li.addEventListener('click', (e) => {
      e.preventDefault();
      const page = li.getAttribute('data-page');
      if (page) {
        navigateTo(page);
      }
    });
  });
}