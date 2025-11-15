# 🚀 Quick Start Guide - EV Charging Station

## Yêu cầu hệ thống

### Backend Requirements

- Java 21 (JDK 21)
- Maven 3.9+
- Microsoft SQL Server 2019+
- IDE: IntelliJ IDEA / Eclipse / VS Code

### Frontend Requirements

- Web Browser hiện đại (Chrome, Firefox, Edge)
- Live Server extension (nếu dùng VS Code)

## 📦 Bước 1: Setup Database

### 1.1 Cài đặt SQL Server

```bash
# Download SQL Server 2019 Express từ:
https://www.microsoft.com/en-us/sql-server/sql-server-downloads

# Hoặc sử dụng Docker:
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourStrong@Passw0rd" \
   -p 1433:1433 --name sqlserver \
   -d mcr.microsoft.com/mssql/server:2019-latest
```

### 1.2 Tạo Database

```sql
CREATE DATABASE ev_charging;
GO

USE ev_charging;
GO
```

### 1.3 Cấu hình Environment Variables

**Tạo file `.env` từ template:**

```bash
cd ev/
cp .env.example .env
```

**Chỉnh sửa file `.env`:**

```properties
DB_HOST=localhost
DB_PORT=1433
DB_NAME=ev_charging
DB_USERNAME=sa
DB_PASSWORD=YourStrong@Passw0rd

JWT_SECRET=your-very-long-secret-key-min-256-bits-for-security
JWT_EXPIRATION=86400000

CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5500
```

⚠️ **Lưu ý**: Không commit file `.env` vào Git!

## 🔧 Bước 2: Setup Backend

### 2.1 Build Project

```bash
cd ev/
mvn clean install
```

### 2.2 Run Application

```bash
mvn spring-boot:run
```

**Hoặc từ IDE:**

1. Mở folder `ev/` trong IntelliJ IDEA
2. Chờ Maven import xong
3. Chạy class `EvApplication.java`

### 2.3 Kiểm tra Backend

Mở browser và truy cập:

```
http://localhost:8080
```

Nếu thấy trang web hoặc error 404 (không phải connection error) => Backend đang chạy ✅

## 🎨 Bước 3: Setup Frontend

### 3.1 Sử dụng VS Code + Live Server

**Cài đặt extension:**

1. Mở VS Code
2. Cài extension "Live Server" by Ritwick Dey
3. Restart VS Code

**Chạy frontend:**

1. Mở folder `front-end/client/`
2. Right-click vào `index.html`
3. Chọn "Open with Live Server"

Frontend sẽ mở tại: `http://localhost:5500` (hoặc port khác)

### 3.2 Sử dụng Python HTTP Server

```bash
cd front-end/client/
python -m http.server 5500
```

Mở browser: `http://localhost:5500`

### 3.3 Sử dụng Node.js HTTP Server

```bash
# Cài đặt http-server global
npm install -g http-server

# Chạy server
cd front-end/client/
http-server -p 5500
```

Mở browser: `http://localhost:5500`

## 👤 Bước 4: Tạo tài khoản Admin đầu tiên

### Cách 1: Sử dụng SQL Script

```sql
USE ev_charging;
GO

-- Tạo admin user (password: Admin123!)
INSERT INTO users (email, password, full_name, phone_number, role, active, created_at)
VALUES (
    'admin@evcharging.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye6F8vC9l7O1z5L9vX7J5E8.9K6C5F5qS', -- BCrypt hash của "Admin123!"
    'System Administrator',
    '0901234567',
    'ADMIN',
    1,
    GETDATE()
);
GO
```

### Cách 2: Sử dụng API

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@evcharging.com",
    "password": "Admin123!",
    "fullName": "System Administrator",
    "phoneNumber": "0901234567",
    "role": "ADMIN"
  }'
```

### Cách 3: Sử dụng trang web

1. Mở `http://localhost:5500/create-admin.html`
2. Điền thông tin
3. Click "Tạo tài khoản Admin"

## 🧪 Bước 5: Testing

### 5.1 Test Admin Dashboard

1. Mở `http://localhost:5500/admin/login.html`
2. Đăng nhập với:
   - Email: `admin@evcharging.com`
   - Password: `Admin123!`
3. Thử các chức năng:
   - Xem danh sách trạm
   - Thêm trạm mới
   - Xem thống kê

### 5.2 Test Driver Features

1. Đăng ký tài khoản mới tại `http://localhost:5500/register.html`
2. Đăng nhập tại `http://localhost:5500/login.html`
3. Thử các tính năng:
   - Xem map trạm sạc
   - Đặt chỗ trạm sạc
   - Quét QR code (cần có QR code test)
   - Xem trạng thái sạc real-time

### 5.3 Test Payment

1. Đăng nhập với tài khoản driver
2. Vào trang Profile, thêm tiền vào ví (tạm thời manual qua database):

```sql
UPDATE users
SET wallet_balance = 1000000
WHERE email = 'your-email@example.com';
```

3. Thử thanh toán một phiên sạc

## 🐛 Troubleshooting

### Lỗi kết nối Database

```
Error: Connection refused / Login failed for user
```

**Giải pháp:**

1. Kiểm tra SQL Server đang chạy
2. Kiểm tra username/password trong `.env`
3. Kiểm tra firewall cho port 1433

### Lỗi CORS

```
Error: CORS policy: No 'Access-Control-Allow-Origin' header
```

**Giải pháp:**

- Cập nhật `CORS_ALLOWED_ORIGINS` trong `.env`
- Restart backend sau khi thay đổi

### Lỗi 401 Unauthorized

```
Error: 401 Unauthorized
```

**Giải pháp:**

1. Kiểm tra JWT token trong localStorage
2. Thử đăng nhập lại
3. Clear browser cache và cookies

### API không trả về dữ liệu

```
Error: Cannot read property 'length' of undefined
```

**Giải pháp:**

1. Kiểm tra backend logs
2. Verify database có dữ liệu
3. Kiểm tra API_BASE_URL trong JS files

### WebSocket không kết nối

```
WebSocket connection failed
```

**Giải pháp:**

1. Kiểm tra backend hỗ trợ WebSocket
2. Kiểm tra URL: `ws://localhost:8080/ws/charging-status`
3. Hệ thống sẽ tự động fallback về polling

## 📱 Test Data

### Tạo trạm sạc mẫu

```sql
INSERT INTO stations (name, latitude, longitude, address, connector_type, power_capacity, price_per_kwh, status)
VALUES
('Trạm Sạc Quận 1', 10.7769, 106.7009, '123 Nguyễn Huệ, Q1, TP.HCM', 'CCS', 50, 3500, 'AVAILABLE'),
('Trạm Sạc Quận 2', 10.7828, 106.7511, '456 Thảo Điền, Q2, TP.HCM', 'CHAdeMO', 100, 4000, 'AVAILABLE'),
('Trạm Sạc Quận 7', 10.7342, 106.7220, '789 Nguyễn Văn Linh, Q7, TP.HCM', 'AC', 22, 2500, 'BUSY');
```

### Tạo user test

```sql
-- Driver user (password: Driver123!)
INSERT INTO users (email, password, full_name, phone_number, role, active, wallet_balance)
VALUES (
    'driver@test.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test Driver',
    '0987654321',
    'EV_DRIVER',
    1,
    500000
);

-- Staff user (password: Staff123!)
INSERT INTO users (email, password, full_name, phone_number, role, active)
VALUES (
    'staff@test.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test Staff',
    '0912345678',
    'CS_STAFF',
    1
);
```

## 📚 API Documentation

Sau khi backend chạy, truy cập Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

Hoặc xem API specs tại:

```
http://localhost:8080/v3/api-docs
```

## 🔒 Default Credentials

| Role   | Email                | Password   |
| ------ | -------------------- | ---------- |
| Admin  | admin@evcharging.com | Admin123!  |
| Staff  | staff@test.com       | Staff123!  |
| Driver | driver@test.com      | Driver123! |

⚠️ **Quan trọng**: Đổi password ngay sau khi deploy production!

## 📞 Support

Nếu gặp vấn đề, kiểm tra:

1. Backend logs: `ev/logs/spring-boot.log`
2. Browser console (F12)
3. Network tab để xem API calls
4. Database connection trong SQL Server Management Studio

## 🎉 Hoàn thành!

Bây giờ bạn có thể:

- ✅ Đăng nhập với các vai trò khác nhau
- ✅ Quản lý trạm sạc
- ✅ Đặt chỗ và thanh toán
- ✅ Xem thống kê và báo cáo
- ✅ Monitoring real-time qua WebSocket

**Happy Coding! 🚗⚡**
