import { state, fetchReports } from './data.js';
import { api } from './api-client.js';

export function renderRevenuePage() {
  const main = document.querySelector('.main-content');
  main.innerHTML = `
    <div class="content-header">
      <h1>Quản lý Doanh thu</h1>
    </div>
    <div class="revenue-summary" id="revenueSummary">
      <p>Đang tải...</p>
    </div>
  `;
  
  // Fetch fresh data from API
  Promise.all([
    fetchReports(),
    api.get('/admin/statistics/revenue?period=DAILY').catch(() => ({ totalRevenue: 0 })),
    api.get('/admin/statistics/revenue?period=MONTHLY').catch(() => ({ totalRevenue: 0 }))
  ]).then(([_, dailyData, monthlyData]) => {
    const summary = document.getElementById('revenueSummary');
    if (!summary) return;
    
    const dailyRevenue = dailyData.totalRevenue || state.reports.dailyRevenue;
    const monthlyRevenue = monthlyData.totalRevenue || state.reports.monthlyRevenue;
    
    summary.innerHTML = `
      <div class="revenue-card">
        <h3>Doanh thu hôm nay</h3>
        <p class="revenue-amount">${dailyRevenue.toLocaleString()}đ</p>
        <span class="revenue-change positive">Real-time data</span>
      </div>
      <div class="revenue-card">
        <h3>Doanh thu tuần này</h3>
        <p class="revenue-amount">${(dailyRevenue * 7).toLocaleString()}đ</p>
        <span class="revenue-change positive">Estimate</span>
      </div>
      <div class="revenue-card">
        <h3>Doanh thu tháng này</h3>
        <p class="revenue-amount">${monthlyRevenue.toLocaleString()}đ</p>
        <span class="revenue-change positive">Real-time data</span>
      </div>
    `;
  }).catch(error => {
    const summary = document.getElementById('revenueSummary');
    if (summary) summary.innerHTML = `<p style="color: red;">Lỗi: ${error.message}</p>`;
  });
}