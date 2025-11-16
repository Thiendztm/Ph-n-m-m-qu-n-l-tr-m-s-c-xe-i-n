# EV Charging Station - Comprehensive Rebuild Status

## Date: November 16, 2025

---

## 📊 PHÂN TÍCH KIẾN TRÚC HIỆN TẠI

### Backend APIs (✅ Đã có)

```
✅ POST /api/auth/register
✅ POST /api/auth/login
✅ POST /api/auth/refresh
✅ GET  /api/stations
✅ GET  /api/stations/{id}/available-chargers
✅ POST /api/stations/{stationId}/chargers/{chargerId}/book
✅ POST /api/stations/{stationId}/chargers/{chargerId}/start-charging
✅ POST /api/payment/wallet
✅ GET  /api/profile/wallet
✅ GET  /api/profile
✅ WS   /ws/charging/{sessionId}
⚠️  GET  /api/charging/history (có nhưng chưa test)
⚠️  GET  /api/charging/session/{sessionId} (có nhưng chưa test)
```

### Frontend Structure (✅ Đã có)

```
front-end/client/
├── index.html (Map + Booking)
├── payment.html (Payment processing)
├── profile.html (User profile + wallet)
├── charging-history.html (History list)
├── session-detail.html (Session details)
├── qr-scanner.html (QR code scanner)
├── charging-status.html (Real-time charging)
├── login.html / register.html
├── staff/ (Staff dashboard)
├── admin/ (Admin dashboard)
└── src/js/
    ├── map.js
    ├── payment.js
    ├── profile.js
    ├── charging-history.js
    ├── session-detail.js
    ├── charging-status.js
    ├── auth.js
    ├── auth-check.js
    ├── navbar.js
    └── api-client.js
```

---

## ✅ CÁC PHẦN ĐÃ HOÀN THÀNH

### 1. Authentication & Authorization

- ✅ Login/Register với email validation
- ✅ JWT token storage (`accessToken` + `refreshToken`)
- ✅ Auto-refresh token on 401
- ✅ Protected routes với auth-check.js
- ✅ Navbar hiển thị user info khi đăng nhập
- ✅ Logout clear tokens

### 2. Map & Booking Flow

- ✅ Leaflet map với OpenStreetMap
- ✅ Load stations từ API `/api/stations`
- ✅ Filter chỉ hiển thị stations có `availableChargers > 0`
- ✅ Geolocation để tìm vị trí user
- ✅ Booking modal với datetime picker
- ✅ Call API `/api/stations/{stationId}/chargers/{chargerId}/book`
- ✅ Lưu `bookingStation` vào localStorage
- ✅ Redirect sang `payment.html` sau khi book thành công

### 3. Payment Flow

- ✅ Đọc `bookingStation` từ localStorage
- ✅ Hiển thị thông tin trạm, connector, giá
- ✅ Chọn gói sạc (Linh hoạt / Một lần / Tháng)
- ✅ Fetch wallet balance từ `/api/profile/wallet`
- ✅ Thanh toán qua `/api/payment/wallet`
- ✅ Update số dư ví sau thanh toán
- ✅ Clear session và redirect về index sau thanh toán thành công

### 4. Font & Styling

- ✅ Local Inter font với Vietnamese support
- ✅ `@charset "UTF-8"` trong CSS
- ✅ Responsive design
- ✅ Dark mode elements

### 5. API Standardization

- ✅ Tất cả JS files dùng `window.API_BASE_URL || 'http://localhost:8080/api'`
- ✅ Token usage: `accessToken` (fallback `jwt_token`)
- ✅ Consistent headers: `Authorization: Bearer {token}`

---

## ❌ CÁC PHẦN CHƯA HOÀN CHỈNH / CẦN BUILD LẠI

### 1. QR Scanner → Charging Session

**Vấn đề:**

- ✅ QR scanner UI đã có
- ❌ Chưa kết nối đúng API endpoint
- ❌ API endpoint trong qr-scanner.html: `/stations/start-session` (thiếu `/api/`)
- ❌ Response handling chưa robust

**Cần fix:**

```javascript
// qr-scanner.html line ~787
// Sai:
fetch(`${API_URL}/stations/start-session`, ...)

// Đúng:
fetch(`${API_BASE_URL}/stations/${stationId}/chargers/${chargerId}/start-charging`, ...)
```

**Expected flow:**

1. User quét QR → lấy `chargerId` và `stationId`
2. Call `POST /api/stations/{stationId}/chargers/{chargerId}/start-charging`
3. Backend tạo `PhienSac` với status `ACTIVE`
4. Response: `{success: true, sessionId, qrCode, startTime}`
5. Lưu `sessionId` vào localStorage
6. Redirect: `charging-status.html?sessionId={sessionId}`

### 2. Real-time Charging Status

**Hiện trạng:**

- ✅ charging-status.html UI đã có
- ✅ WebSocket client code đã có
- ⚠️ WebSocket connection logic chưa chạy đúng
- ❌ Fallback polling chưa được test

**Cần fix:**

- Import `api-client.js` đúng cách
- Kết nối WebSocket: `ws://localhost:8080/ws`
- Subscribe topic: `/topic/charging/{sessionId}`
- Update UI mỗi 5s: SOC%, kWh, cost, time remaining
- Stop charging button call `/api/stations/stop-session`

### 3. Charging History

**Vấn đề:**

- ✅ UI đã có (charging-history.html)
- ❌ Vẫn dùng `generateMockData()` thay vì API
- ❌ API endpoint chưa được verify: `/api/charging/history?userId={userId}`

**Cần fix:**

```javascript
// Ensure API returns:
{
  "sessions": [
    {
      "sessionId": "SESSION-1001",
      "stationName": "Trạm A",
      "startTime": "2025-11-15T10:00:00",
      "endTime": "2025-11-15T11:30:00",
      "energyConsumed": 45.5,
      "totalCost": 159250,
      "startSoc": 20,
      "endSoc": 95,
      "status": "COMPLETED"
    }
  ]
}
```

### 4. Session Detail Page

**Hiện trạng:**

- ✅ UI đã có
- ✅ Fallback đọc `currentSessionId` từ localStorage
- ❌ Vẫn dùng `generateMockSession()` khi API fail

**Cần verify:**

- GET `/api/charging/session/{sessionId}` trả đúng format
- Hiển thị SOC progress, timeline, cost breakdown

### 5. Profile Page

**Vấn đề:**

- ✅ UI đã có
- ⚠️ Fetch wallet từ `/api/profile/wallet` OK
- ⚠️ Fetch user info từ `/api/profile` chưa verify
- ❌ Không lưu thông tin xe (biển số, model, loại sạc)

**Cần thêm:**

- Form nhập thông tin xe
- API: `PUT /api/profile/vehicle` để lưu

```json
{
  "licensePlate": "30A-12345",
  "model": "Tesla Model 3",
  "connectorType": "CCS2",
  "batteryCapacity": 75
}
```

### 6. Staff Dashboard

**Vấn đề:**

- ✅ Basic UI đã có (staff/index.html)
- ❌ Chưa có chức năng thực sự
- ❌ Thiếu API endpoints cho Staff

**Cần build:**

**A. Quản lý phiên sạc tại trạm**

```javascript
// API cần có:
POST /api/staff/sessions/start
  Body: { userId, chargerId, stationId }

POST /api/staff/sessions/{sessionId}/stop
  Body: { endSoc, finalCost }

GET /api/staff/station/{stationId}/status
  Response: { active sessions, chargers status }
```

**B. UI cần có:**

- Danh sách phiên sạc đang active
- Nút Start / Stop session cho từng charger
- Hiển thị real-time SOC của các session
- Form xác nhận thanh toán tại chỗ

### 7. Admin Dashboard

**Vấn đề:**

- ✅ Basic UI đã có (admin/index.html)
- ❌ Chưa có CRUD cho stations/chargers
- ❌ Chưa có user management
- ❌ Chưa có báo cáo doanh thu

**Cần build:**

**A. Quản lý trạm & bộ sạc**

```javascript
// Stations
GET / api / admin / stations;
POST / api / admin / stations;
PUT / api / admin / stations / { id };
DELETE / api / admin / stations / { id };

// Chargers
GET / api / admin / stations / { stationId } / chargers;
POST / api / admin / stations / { stationId } / chargers;
PUT / api / admin / chargers / { id };
DELETE / api / admin / chargers / { id };
```

**B. Quản lý users**

```javascript
GET / api / admin / users;
PUT / api / admin / users / { id } / role;
DELETE / api / admin / users / { id };
```

**C. Báo cáo & thống kê**

```javascript
GET /api/admin/reports/revenue?from=...&to=...
  Response: {
    totalRevenue, sessionCount,
    byStation: [...], byDate: [...]
  }

GET /api/admin/reports/usage
  Response: {
    peakHours, averageDuration,
    topStations: [...]
  }
```

### 8. Subscription Plans

**Vấn đề:**

- ✅ UI hiển thị gói sạc trên payment.html
- ❌ Backend chưa có entity/table cho Plans
- ❌ Chưa có logic áp dụng gói

**Cần build:**

- Backend: `SubscriptionPlan` entity
- API: `/api/plans` (list plans)
- API: `/api/profile/subscribe` (subscribe to plan)
- Logic tính giá theo plan khi thanh toán

### 9. Notifications

**Vấn đề:**

- ❌ Chưa có push notifications
- ❌ Chưa có email notifications

**Cần build:**

- WebSocket cho notifications: `/topic/notifications/{userId}`
- Email service cho:
  - Booking confirmation
  - Charging complete
  - Payment receipt

---

## 🔧 DANH SÁCH CÁC FILE CẦN SỬA/TẠO MỚI

### Files cần sửa (Priority 1):

```
1. front-end/client/qr-scanner.html
   - Fix API endpoint cho start-charging
   - Robust error handling

2. front-end/client/charging-status.html
   - Fix WebSocket connection
   - Implement polling fallback

3. front-end/client/src/js/charging-history.js
   - Remove mock data
   - Proper API integration

4. front-end/client/src/js/session-detail.js
   - Remove mock data
   - Handle API errors gracefully

5. front-end/client/src/js/profile.js
   - Add vehicle info form & API
   - Fix profile fetch
```

### Files cần tạo mới (Priority 2):

```
1. front-end/client/staff/js/sessions.js
   - Start/stop session logic
   - Real-time session monitoring

2. front-end/client/staff/js/payments.js
   - At-site payment confirmation

3. front-end/client/admin/js/stations-management.js
   - CRUD operations for stations

4. front-end/client/admin/js/users-management.js
   - User list, edit role, delete

5. front-end/client/admin/js/reports.js
   - Revenue charts
   - Usage analytics
```

### Backend cần verify/thêm:

```
1. ChargingController.java
   - Verify GET /charging/history
   - Verify GET /charging/session/{id}

2. StaffController.java (cần tạo mới?)
   - POST /staff/sessions/start
   - POST /staff/sessions/{id}/stop
   - GET /staff/station/{id}/status

3. AdminController.java
   - CRUD stations/chargers
   - User management
   - Reports endpoints
```

---

## 📝 CHECKLIST TESTING

### Driver Flow:

```
☐ 1. Đăng ký tài khoản mới
☐ 2. Đăng nhập
☐ 3. Xem bản đồ, filter trạm khả dụng
☐ 4. Đặt chỗ một trạm
☐ 5. Thanh toán qua EV Wallet
☐ 6. Quét QR code để bắt đầu sạc
☐ 7. Xem real-time charging status
☐ 8. Dừng sạc
☐ 9. Xem lịch sử sạc
☐ 10. Xem chi tiết một session
☐ 11. Cập nhật thông tin cá nhân
☐ 12. Nhập thông tin xe
```

### Staff Flow:

```
☐ 1. Đăng nhập với role STAFF
☐ 2. Xem danh sách phiên sạc tại trạm
☐ 3. Start session cho khách
☐ 4. Monitor real-time SOC
☐ 5. Stop session
☐ 6. Xác nhận thanh toán tại chỗ
☐ 7. Cập nhật trạng thái trạm (online/offline)
☐ 8. Báo cáo sự cố lên Admin
```

### Admin Flow:

```
☐ 1. Đăng nhập với role ADMIN
☐ 2. Thêm trạm sạc mới
☐ 3. Thêm charger vào trạm
☐ 4. Xem toàn bộ users
☐ 5. Chỉnh sửa role user
☐ 6. Xóa user
☐ 7. Xem báo cáo doanh thu
☐ 8. Xem báo cáo usage analytics
☐ 9. Quản lý subscription plans
```

---

## 🎯 KẾ HOẠCH THỰC HIỆN (PRIORITY ORDER)

### Phase 1: Critical Fixes (Ngay lập tức)

1. ✅ Fix API base URL consistency (DONE)
2. ✅ Fix token standardization (DONE)
3. ✅ Fix booking → payment flow (DONE)
4. ⏳ Fix QR scanner API endpoint
5. ⏳ Fix charging-status WebSocket
6. ⏳ Remove mock data từ history/profile

### Phase 2: Core Features (1-2 ngày)

7. Build Staff dashboard sessions management
8. Verify & fix all Backend APIs
9. Add vehicle info to Profile
10. Implement notifications

### Phase 3: Admin Features (2-3 ngày)

11. Admin CRUD stations/chargers
12. Admin user management
13. Admin reports & analytics
14. Subscription plans backend & UI

### Phase 4: Polish & Testing (1 ngày)

15. Code cleanup & refactor
16. End-to-end testing
17. Performance optimization
18. Documentation

---

## 📌 NOTES & DECISIONS

### API Base URL Strategy:

```javascript
// Mọi file đều dùng:
const API_BASE_URL = window.API_BASE_URL || "http://localhost:8080/api";

// Set global tại navbar.js:
window.API_BASE_URL = "http://localhost:8080/api";
```

### Token Strategy:

```javascript
// Primary key: 'accessToken'
// Fallback: 'jwt_token' (for backward compatibility)
const token =
  localStorage.getItem("accessToken") || localStorage.getItem("jwt_token");
```

### Session Flow:

```
1. Booking (optional) → save bookingId
2. QR Scan → start session → save sessionId
3. Real-time updates via WebSocket
4. Stop session → payment (if not pre-paid)
5. Save to history
```

### Data Persistence:

```javascript
localStorage keys:
- accessToken
- refreshToken
- userId
- userName
- userEmail
- userRole
- walletBalance (cached)
- bookingStation (temp)
- currentSessionId (temp)
- currentBookingId (temp)
```

---

**Status:** Đang trong quá trình rebuild - Phase 1 hoàn thành 70%
**Next step:** Fix QR scanner API endpoint và WebSocket connection
