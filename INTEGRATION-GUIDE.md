# 🔗 Frontend-Backend Integration Guide

## ✅ Tích hợp đã hoàn thành

Frontend đã được tích hợp vào Spring Boot thông qua **static resources**.

### 📂 Cấu trúc thư mục

```
EV-Charging-Station/
├── front-end/client/        # Frontend source (development)
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── admin/
│   ├── staff/
│   └── src/
│       ├── css/
│       ├── js/
│       └── img/
│
├── ev/src/main/resources/
│   └── static/              # Frontend files served by Spring Boot (production)
│       ├── index.html       # Auto-served at http://localhost:8080/
│       ├── login.html       # Available at http://localhost:8080/login.html
│       └── ...              # All other frontend files
│
└── copy-frontend.bat        # Script để copy frontend files
```

---

## 🔄 Workflow: Phát triển Frontend

### 1️⃣ **Phát triển**

Làm việc trong thư mục `front-end/client/`:

```bash
cd front-end/client
# Edit HTML/CSS/JS files
```

### 2️⃣ **Copy sang Spring Boot**

Sau khi có thay đổi, copy files:

```bash
# Option 1: Dùng script có sẵn
copy-frontend.bat

# Option 2: Copy thủ công
xcopy /E /I /Y "front-end\client" "ev\src\main\resources\static"
```

### 3️⃣ **Test với Spring Boot**

```bash
cd ev
mvn spring-boot:run
```

Truy cập: **http://localhost:8080**

---

## 🌐 API Endpoints

### Frontend URLs (served by Spring Boot)

- **Homepage**: http://localhost:8080/
- **Login**: http://localhost:8080/login.html
- **Register**: http://localhost:8080/register.html
- **Admin**: http://localhost:8080/admin/
- **Staff**: http://localhost:8080/staff/

### Backend APIs

- **Auth**: http://localhost:8080/api/auth/\*
  - POST `/api/auth/register` - Đăng ký
  - POST `/api/auth/login` - Đăng nhập
  - POST `/api/auth/refresh` - Refresh token
- **Driver**: http://localhost:8080/api/driver/\*
- **Staff**: http://localhost:8080/api/staff/\*
- **Admin**: http://localhost:8080/api/admin/\*

---

## 🔐 Security Configuration

### Public endpoints (không cần login):

```
/                         # Root page
/index.html               # Home
/login.html               # Login page
/register.html            # Register page
/admin/**                 # Admin pages
/staff/**                 # Staff pages
/src/**                   # Static resources (CSS, JS, images)
/api/auth/**              # Authentication APIs
```

### Protected endpoints:

```
/api/driver/**            # Cần role: EV_DRIVER
/api/staff/**             # Cần role: CS_STAFF
/api/admin/**             # Cần role: ADMIN
```

---

## 📝 Lưu ý quan trọng

### ✅ API URL trong Frontend

File `src/js/auth.js` đã được cập nhật:

```javascript
// OLD (khi chạy riêng lẻ):
const API_BASE_URL = "http://localhost:8080/api";

// NEW (khi Spring Boot serve):
const API_BASE_URL = "/api"; // Relative URL
```

### ✅ CORS Configuration

Spring Boot đã cấu hình CORS cho phép:

- Allowed origins: `http://localhost:8080`
- Allowed methods: `GET, POST, PUT, DELETE, PATCH, OPTIONS`
- Credentials: `true` (cho phép cookie và JWT token)

### ✅ Maven Build

Maven tự động copy 36 files khi build:

```
[INFO] Copying 36 resources from src\main\resources to target\classes
```

---

## 🚀 Deployment Checklist

### Development

- [x] Frontend code trong `front-end/client/`
- [x] Run `copy-frontend.bat` sau mỗi thay đổi
- [x] Test trên `http://localhost:8080`

### Production

- [x] Copy frontend files vào `src/main/resources/static/`
- [x] Build project: `mvn clean package`
- [x] JAR file sẽ chứa cả frontend và backend
- [x] Deploy JAR file: `java -jar ev-0.0.1-SNAPSHOT.jar`

---

## 🐛 Troubleshooting

### Frontend không hiển thị?

1. Kiểm tra files có trong `ev/src/main/resources/static/`:
   ```bash
   ls ev/src/main/resources/static/
   ```
2. Clear Maven cache và rebuild:
   ```bash
   mvn clean
   mvn spring-boot:run
   ```

### API call bị CORS error?

- Check `application.properties`: `cors.allowed-origins=http://localhost:8080`
- Đảm bảo frontend dùng relative URL (`/api`) thay vì absolute URL

### 404 Not Found?

- Kiểm tra SecurityConfig đã permit tất cả static files:
  ```java
  .requestMatchers("/", "/index.html", "/src/**", "/admin/**").permitAll()
  ```

---

## 📚 Tài liệu tham khảo

- [Spring Boot Static Content](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.static-content)
- [Spring Security Configuration](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html)
