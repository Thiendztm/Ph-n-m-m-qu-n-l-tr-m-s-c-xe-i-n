export function renderSupportPage() {
    const main = document.querySelector('.main-content');
    main.innerHTML = `
      <div class="content-header">
        <h1>Quản lý Hỗ trợ</h1>
      </div>
      <div class="support-grid">
        <div class="support-card">
          <div class="support-icon"><i class="fa-solid fa-ticket"></i></div>
          <div class="support-content">
            <h3>Tickets đang mở</h3>
            <p class="support-value">12</p>
          </div>
        </div>
        <div class="support-card">
          <div class="support-icon"><i class="fa-solid fa-check-circle"></i></div>
          <div class="support-content">
            <h3>Tickets đã giải quyết</h3>
            <p class="support-value">156</p>
          </div>
        </div>
        <div class="support-card">
          <div class="support-icon"><i class="fa-solid fa-clock"></i></div>
          <div class="support-content">
            <h3>Thời gian phản hồi TB</h3>
            <p class="support-value">2.5 giờ</p>
          </div>
        </div>
      </div>
    `;
  }