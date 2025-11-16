# EV Charging Station - Phase 1 Rebuild Summary

## Date: November 16, 2025

---

## ✅ ĐÃ HOÀN THÀNH TRONG SESSION NÀY

### 1. API Base URL Standardization (24 files)

**Mục đích:** Tất cả JS đều dùng `window.API_BASE_URL` để dễ config

**Files đã sửa:**

```
✅ front-end/client/src/js/map.js
✅ front-end/client/src/js/payment.js
✅ front-end/client/src/js/profile.js
✅ front-end/client/src/js/auth.js
✅ front-end/client/src/js/charging-history.js
✅ front-end/client/src/js/session-detail.js
✅ front-end/client/src/js/api-client.js
✅ front-end/client/src/js/auth-check.js
✅ front-end/client/src/js/navbar.js
✅ front-end/client/staff/js/data.js
✅ front-end/client/staff/js/login-staff.js
✅ front-end/client/admin/js/api-client.js
✅ ev/src/main/resources/static/... (all mirrors)
```

**Pattern sử dụng:**

```javascript
const API_BASE_URL = window.API_BASE_URL || "http://localhost:8080/api";
```

---

### 2. Token Standardization (14 files)

**Mục đích:** Ưu tiên `accessToken`, fallback `jwt_token`

**Thay đổi:**

```javascript
// Old:
const token = localStorage.getItem("jwt_token");

// New:
const token =
  localStorage.getItem("accessToken") || localStorage.getItem("jwt_token");

// Logout clears both:
localStorage.removeItem("accessToken");
localStorage.removeItem("jwt_token");
```

---

### 3. Booking → Payment Data Flow

**Đã fix:**

- `map.js`: Lưu đầy đủ `bookingStation` object vào localStorage
- `payment.js`: Đọc và hiển thị thông tin station
- Lưu `sessionId`/`bookingId` nếu backend trả về
- Redirect đúng sau khi book thành công

**Data structure:**

```javascript
{
  id: stationId,
  name: "Trạm A",
  connectorDisplay: "CCS2",
  priceDisplay: "3,500đ/kWh",
  price: 3500
}
```

---

### 4. Session Detail & History Fallback

**Session Detail:**

```javascript
// Đọc sessionId từ URL hoặc localStorage
let sessionId =
  urlParams.get("id") ||
  localStorage.getItem("currentSessionId") ||
  localStorage.getItem("currentBookingId");
```

**Charging History:**

```javascript
// Hỗ trợ cả có và không có userId param
const endpoint = userId
  ? `${API_BASE_URL}/charging/history?userId=${userId}`
  : `${API_BASE_URL}/charging/history`;

// Accept cả array và object response
const sessions = Array.isArray(data) ? data : data.sessions || [];
```

---

### 5. QR Scanner API Fix

**Vấn đề cũ:**

```javascript
// Sai - endpoint không tồn tại:
fetch(`${API_URL}/stations/start-session`, ...)
```

**Đã sửa:**

```javascript
// Đúng - theo backend API:
const stationId = qrData.stationId;
const chargerId = qrData.chargerId;

fetch(`${API_URL}/stations/${stationId}/chargers/${chargerId}/start-charging`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify({}),
});
```

**Expected QR format:**

```json
{
  "stationId": 1,
  "chargerId": 5
}
```

---

### 6. Copy Script Enhancement

**File:** `copy-frontend.bat`

```batch
@echo off
setlocal

REM Copy client assets
xcopy /E /I /Y "front-end\client\*" "ev\src\main\resources\static\." >nul

REM Copy fonts
xcopy /E /I /Y "front-end\fonts\*" "ev\src\main\resources\static\fonts\." >nul

echo Done! Frontend files copied successfully.
endlocal
pause
```

---

### 7. Local Font System

**Structure:**

```
front-end/fonts/
├── fonts.css
└── Inter/
    └── InterVariable.ttf
```

**fonts.css:**

```css
@charset "UTF-8";

@font-face {
  font-family: "Inter";
  src: url("Inter/InterVariable.ttf") format("truetype-variations");
  font-weight: 300 700;
  font-display: swap;
}
```

**Integration:**

```html
<link rel="stylesheet" href="../fonts/fonts.css" />
```

---

## 🎯 NHỮNG GÌ CÒN LẠI CẦN LÀM

### Priority 1: Critical Bugs

#### A. Charging Status WebSocket

**File:** `front-end/client/charging-status.html`
**Vấn đề:** WebSocket connection chưa stable

**Cần verify:**

1. Backend WebSocket đang chạy: `ws://localhost:8080/ws`
2. Topic subscription: `/topic/charging/{sessionId}`
3. Message format đúng với `ChargingStatusUpdate` DTO

**Test command:**

```javascript
// Trong browser console tại charging-status.html:
realtimeConn.connect();
```

#### B. Remove Mock Data

**Files cần fix:**

```
1. charging-history.js
   - Line ~70: generateMockData()
   - Chỉ dùng khi API thất bại thật

2. session-detail.js
   - Line ~35: generateMockSession()
   - Chỉ dùng khi API thất bại thật
```

**Solution:**

- Log lỗi rõ ràng khi API fail
- Hiển thị error UI thay vì mock data
- Guide user làm gì tiếp theo (retry, contact support)

---

### Priority 2: Missing Features

#### C. Profile Vehicle Info

**Cần thêm vào:** `front-end/client/profile.html`

**Form fields:**

```html
<div class="vehicle-section">
  <h3>Thông tin xe</h3>
  <input type="text" id="licensePlate" placeholder="Biển số" />
  <input type="text" id="model" placeholder="Model xe" />
  <select id="connectorType">
    <option value="CCS2">CCS2</option>
    <option value="CHAdeMO">CHAdeMO</option>
    <option value="Type2">Type 2</option>
  </select>
  <input
    type="number"
    id="batteryCapacity"
    placeholder="Dung lượng pin (kWh)"
  />
  <button onclick="saveVehicleInfo()">Lưu</button>
</div>
```

**API call:**

```javascript
async function saveVehicleInfo() {
  await fetch(`${API_BASE_URL}/profile/vehicle`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      licensePlate,
      model,
      connectorType,
      batteryCapacity,
    }),
  });
}
```

---

#### D. Staff Dashboard - Sessions Management

**File mới:** `front-end/client/staff/js/sessions.js`

**Core functions cần có:**

```javascript
// 1. Fetch active sessions at station
async function loadActiveSessions(stationId) {
  const response = await fetch(
    `${API_BASE_URL}/staff/station/${stationId}/status`,
    {
      headers: { Authorization: `Bearer ${token}` },
    }
  );
  const data = await response.json();
  renderSessions(data.sessions);
}

// 2. Start session for walk-in customer
async function startSession(chargerId, customerId) {
  await fetch(`${API_BASE_URL}/staff/sessions/start`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ chargerId, userId: customerId }),
  });
}

// 3. Stop session
async function stopSession(sessionId, endSoc, finalCost) {
  await fetch(`${API_BASE_URL}/staff/sessions/${sessionId}/stop`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ endSoc, finalCost }),
  });
}

// 4. Confirm at-site payment
async function confirmPayment(sessionId, amount, method) {
  await fetch(`${API_BASE_URL}/staff/payments/confirm`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ sessionId, amount, method }),
  });
}
```

**UI Components:**

```html
<div class="active-sessions">
  <h2>Phiên sạc đang hoạt động</h2>
  <table id="sessionsTable">
    <thead>
      <tr>
        <th>Charger</th>
        <th>Khách hàng</th>
        <th>SOC</th>
        <th>Thời gian</th>
        <th>Chi phí</th>
        <th>Hành động</th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
</div>
```

---

#### E. Admin Dashboard - Stations CRUD

**File mới:** `front-end/client/admin/js/stations-management.js`

**Core functions:**

```javascript
// List all stations
async function loadStations() {
  const response = await fetch(`${API_BASE_URL}/admin/stations`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const stations = await response.json();
  renderStationsTable(stations);
}

// Add new station
async function addStation(stationData) {
  await fetch(`${API_BASE_URL}/admin/stations`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(stationData),
  });
}

// Update station
async function updateStation(stationId, stationData) {
  await fetch(`${API_BASE_URL}/admin/stations/${stationId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(stationData),
  });
}

// Delete station
async function deleteStation(stationId) {
  if (!confirm("Xác nhận xóa trạm này?")) return;

  await fetch(`${API_BASE_URL}/admin/stations/${stationId}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
}
```

---

#### F. Admin Dashboard - Revenue Reports

**File mới:** `front-end/client/admin/js/reports.js`

**Core functions:**

```javascript
// Fetch revenue data
async function loadRevenueReport(from, to) {
  const response = await fetch(
    `${API_BASE_URL}/admin/reports/revenue?from=${from}&to=${to}`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  const data = await response.json();
  renderRevenueChart(data);
}

// Render chart using Chart.js
function renderRevenueChart(data) {
  const ctx = document.getElementById("revenueChart").getContext("2d");
  new Chart(ctx, {
    type: "line",
    data: {
      labels: data.byDate.map((d) => d.date),
      datasets: [
        {
          label: "Doanh thu",
          data: data.byDate.map((d) => d.revenue),
          borderColor: "#4CAF50",
        },
      ],
    },
  });
}
```

---

## 📋 BACKEND APIs CẦN VERIFY/TẠO

### Driver APIs (cần verify):

```
✅ POST /api/stations/{stationId}/chargers/{chargerId}/book
✅ POST /api/stations/{stationId}/chargers/{chargerId}/start-charging
⚠️  GET  /api/charging/history?userId={userId}
⚠️  GET  /api/charging/session/{sessionId}
❌ PUT  /api/profile/vehicle (chưa có - cần tạo)
```

### Staff APIs (cần tạo):

```
❌ POST /api/staff/sessions/start
❌ POST /api/staff/sessions/{sessionId}/stop
❌ GET  /api/staff/station/{stationId}/status
❌ POST /api/staff/payments/confirm
```

### Admin APIs (cần tạo):

```
❌ GET    /api/admin/stations
❌ POST   /api/admin/stations
❌ PUT    /api/admin/stations/{id}
❌ DELETE /api/admin/stations/{id}
❌ GET    /api/admin/stations/{stationId}/chargers
❌ POST   /api/admin/stations/{stationId}/chargers
❌ GET    /api/admin/users
❌ PUT    /api/admin/users/{id}/role
❌ DELETE /api/admin/users/{id}
❌ GET    /api/admin/reports/revenue
❌ GET    /api/admin/reports/usage
```

---

## 🧪 TESTING CHECKLIST

### Driver Flow:

```bash
# 1. Test booking
- Mở index.html
- Chọn một trạm có sẵn trên map
- Click "Đặt chỗ"
- Điền thông tin thời gian
- Submit → redirect payment.html
- Verify: payment page hiển thị đúng thông tin trạm

# 2. Test QR scanning
- Tạo QR code với format: {"stationId":1,"chargerId":5}
- Mở qr-scanner.html
- Quét QR
- Verify: redirect charging-status.html?sessionId=...

# 3. Test real-time charging
- Tại charging-status.html
- Mở DevTools Network tab
- Verify: WebSocket connection established
- Verify: Nhận updates mỗi 5s

# 4. Test history
- Mở charging-history.html
- Verify: Load data từ API, không phải mock
- Click vào một session
- Verify: session-detail.html hiển thị đúng
```

### Staff Flow:

```bash
# 1. Login as staff
- Email: staff@example.com
- Verify: navbar hiển thị staff menu

# 2. View station status
- Mở staff/index.html
- Verify: Hiển thị active sessions

# 3. Start session
- Click "Bắt đầu sạc" cho một charger
- Verify: Session xuất hiện trong danh sách

# 4. Stop session
- Click "Dừng" cho một session
- Verify: Session chuyển sang COMPLETED
```

### Admin Flow:

```bash
# 1. Login as admin
- Email: admin@example.com
- Verify: navbar hiển thị admin dashboard link

# 2. Manage stations
- Mở admin/index.html
- Click "Thêm trạm"
- Điền thông tin → Submit
- Verify: Trạm xuất hiện trong danh sách

# 3. View reports
- Click tab "Báo cáo"
- Select date range
- Verify: Chart hiển thị doanh thu
```

---

## 📦 DELIVERABLES

### Files đã sửa (session này):

1. ✅ All JS files: API base URL standardization
2. ✅ All JS files: Token standardization
3. ✅ map.js: Booking data persistence
4. ✅ payment.js: Read booking data
5. ✅ session-detail.js: localStorage fallback
6. ✅ charging-history.js: Flexible API params
7. ✅ qr-scanner.html: Fix API endpoint
8. ✅ copy-frontend.bat: Enhanced copying
9. ✅ fonts system: Local Inter with Vietnamese

### Files cần tạo mới (next):

1. ⏳ staff/js/sessions.js
2. ⏳ staff/js/payments.js
3. ⏳ admin/js/stations-management.js
4. ⏳ admin/js/users-management.js
5. ⏳ admin/js/reports.js

### Backend cần verify/tạo:

1. ⏳ Verify charging history API
2. ⏳ Verify session detail API
3. ⏳ Create staff APIs
4. ⏳ Create admin APIs
5. ⏳ Add vehicle info endpoint

---

## 🚀 NEXT STEPS

**Immediate (trong 1-2 giờ):**

1. Test QR scanner với backend thật
2. Verify WebSocket connection cho charging-status
3. Remove mock data từ history/profile

**Short-term (1-2 ngày):**

1. Build Staff dashboard sessions management
2. Build Admin stations CRUD
3. Add vehicle info to profile

**Medium-term (3-5 ngày):**

1. Complete Admin reports & analytics
2. Implement notifications system
3. Add subscription plans
4. End-to-end testing

---

**Status:** Phase 1 hoàn thành 80%  
**Next milestone:** Complete Staff & Admin dashboards  
**Target:** Full system integration trong 1 tuần
