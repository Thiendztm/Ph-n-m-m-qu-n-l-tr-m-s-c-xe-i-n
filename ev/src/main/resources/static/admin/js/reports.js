import { state } from './data.js';

export function renderReportsPage() {
  const main = document.querySelector('.main-content');
  main.innerHTML = `
    <div class="content-header">
      <h1>Báo cáo Thống kê</h1>
    </div>
    <div class="reports-grid">
      <div class="report-card">
        <div class="report-icon"><i class="fa-solid fa-money-bill-wave"></i></div>
        <div class="report-content">
          <h3>Doanh thu hôm nay</h3>
          <p class="report-value">${state.reports.dailyRevenue.toLocaleString()}đ</p>
        </div>
      </div>
      <div class="report-card">
        <div class="report-icon"><i class="fa-solid fa-chart-line"></i></div>
        <div class="report-content">
          <h3>Doanh thu tháng</h3>
          <p class="report-value">${state.reports.monthlyRevenue.toLocaleString()}đ</p>
        </div>
      </div>
      <div class="report-card">
        <div class="report-icon"><i class="fa-solid fa-users"></i></div>
        <div class="report-content">
          <h3>Tổng người dùng</h3>
          <p class="report-value">${state.reports.totalUsers}</p>
        </div>
      </div>
      <div class="report-card">
        <div class="report-icon"><i class="fa-solid fa-bolt"></i></div>
        <div class="report-content">
          <h3>Trạm hoạt động</h3>
          <p class="report-value">${state.reports.activeStations}</p>
        </div>
      </div>
      <div class="report-card">
        <div class="report-icon"><i class="fa-solid fa-charging-station"></i></div>
        <div class="report-content">
          <h3>Tổng lần sạc</h3>
          <p class="report-value">${state.reports.totalCharges}</p>
        </div>
      </div>
      <div class="report-card">
        <div class="report-icon"><i class="fa-solid fa-clock"></i></div>
        <div class="report-content">
          <h3>Thời gian TB</h3>
          <p class="report-value">${state.reports.averageSessionTime}</p>
        </div>
      </div>
    </div>
  `;
}