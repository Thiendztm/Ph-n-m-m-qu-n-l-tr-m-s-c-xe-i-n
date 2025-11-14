import { state } from './data.js';

export function renderRevenuePage() {
  const main = document.querySelector('.main-content');
  main.innerHTML = `
    <div class="content-header">
      <h1>Quản lý Doanh thu</h1>
    </div>
    <div class="revenue-summary">
      <div class="revenue-card">
        <h3>Doanh thu hôm nay</h3>
        <p class="revenue-amount">${state.reports.dailyRevenue.toLocaleString()}đ</p>
        <span class="revenue-change positive">+12% so với hôm qua</span>
      </div>
      <div class="revenue-card">
        <h3>Doanh thu tuần này</h3>
        <p class="revenue-amount">${(state.reports.dailyRevenue * 7).toLocaleString()}đ</p>
        <span class="revenue-change positive">+8% so với tuần trước</span>
      </div>
      <div class="revenue-card">
        <h3>Doanh thu tháng này</h3>
        <p class="revenue-amount">${state.reports.monthlyRevenue.toLocaleString()}đ</p>
        <span class="revenue-change positive">+15% so với tháng trước</span>
      </div>
    </div>
  `;
}