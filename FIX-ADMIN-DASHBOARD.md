# 🔧 Hướng dẫn sửa lỗi Admin Dashboard

## 🐛 Vấn đề đã gặp:

1. ❌ **Không thấy danh sách Driver đã đăng ký**
2. ❌ **Không tạo được tài khoản Staff**
3. ❌ **Backend trả về dữ liệu không khớp với frontend**

## ✅ Các sửa đổi đã thực hiện:

### 1. **Sửa mapping dữ liệu trong `data.js`**
   - Backend trả về: `firstName`, `lastName`, `phone`
   - Frontend đang tìm: `fullName`, `phoneNumber`, `active`
   - **Giải pháp**: Cập nhật mapping để ghép `firstName + lastName` thành `name`

### 2. **Tạo module quản lý Staff mới**
   - File: `admin/js/staff-management.js`
   - Tính năng:
     - ✅ Hiển thị danh sách nhân viên (CS_STAFF)
     - ✅ Thêm tài khoản Staff qua API `/api/admin/staff`
     - ✅ Sửa/Xóa tài khoản Staff
     - ✅ Form nhập đầy đủ: email, password, họ tên, SĐT

### 3. **Thêm menu "Nhân viên" vào Admin Dashboard**
   - Thêm menu item với icon `fa-user-tie`
   - Import module `staff-management.js`
   - Thêm routing case `'staff'` trong `main.js`

### 4. **Tạo script SQL tạo tài khoản test**
   - File: `create-admin-user.sql`
   - Tạo sẵn 4 tài khoản:
     - 1 Admin: `admin@evcharging.com`
     - 2 Driver: `driver1@gmail.com`, `driver2@gmail.com`
     - 1 Staff: `staff1@evcharging.com`
   - Password mặc định: `password123`

## 📋 Các bước để test:

### **Bước 1: Tạo tài khoản test trong database**

Chạy file SQL `create-admin-user.sql` trong MySQL:

```bash
mysql -u root -p ev_charging_db < create-admin-user.sql
```

Hoặc trong MySQL Workbench/phpMyAdmin, copy nội dung file và chạy.

### **Bước 2: Đăng nhập Admin Dashboard**

1. Truy cập: **http://localhost:8080/admin/login.html**
2. Đăng nhập với:
   - Email: `admin@evcharging.com`
   - Password: `password123`

### **Bước 3: Kiểm tra tính năng**

#### ✅ **Xem danh sách Người dùng (Driver)**
1. Click menu "Người dùng"
2. Sẽ hiển thị 2 driver: `Nguyễn Văn An`, `Trần Thị Bình`
3. Kiểm tra thông tin: email, SĐT, số dư ví

#### ✅ **Xem danh sách Nhân viên (Staff)**
1. Click menu "Nhân viên" (icon cà vạt)
2. Sẽ hiển thị 1 staff: `Lê Văn Chiến`
3. Có thể sửa/xóa

#### ✅ **Tạo tài khoản Staff mới**
1. Click nút "Thêm nhân viên"
2. Điền form:
   - Email: `staff2@evcharging.com`
   - Password: `password123`
   - Họ và tên: `Phạm Văn Dũng`
   - SĐT: `0923456789`
3. Click "Tạo tài khoản"
4. Kiểm tra danh sách nhân viên đã có tài khoản mới

## 🔍 Kiểm tra API Backend:

### Lấy danh sách Users (cần token)
```bash
curl -X GET "http://localhost:8080/api/admin/users" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Lấy danh sách Driver
```bash
curl -X GET "http://localhost:8080/api/admin/users?role=EV_DRIVER" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Lấy danh sách Staff
```bash
curl -X GET "http://localhost:8080/api/admin/users?role=CS_STAFF" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Tạo tài khoản Staff
```bash
curl -X POST "http://localhost:8080/api/admin/staff" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newstaff@example.com",
    "password": "password123",
    "fullName": "Nguyễn Test",
    "phoneNumber": "0987654321"
  }'
```

## 📁 Cấu trúc file đã thay đổi:

```
admin/
├── index.html                    (Thêm menu "Nhân viên")
├── js/
│   ├── main.js                  (Thêm import staff-management, thêm case 'staff')
│   ├── data.js                  (Sửa mapping: firstName+lastName, phone)
│   └── staff-management.js      (MỚI - Quản lý Staff)
```

## 🔑 Tài khoản test đã tạo:

| Email | Password | Role | Tên |
|-------|----------|------|-----|
| admin@evcharging.com | password123 | ADMIN | Admin System |
| driver1@gmail.com | password123 | EV_DRIVER | Nguyễn Văn An |
| driver2@gmail.com | password123 | EV_DRIVER | Trần Thị Bình |
| staff1@evcharging.com | password123 | CS_STAFF | Lê Văn Chiến |

## 🚀 Tóm tắt:

### ✅ **ĐÃ SỬA:**
1. **Mapping dữ liệu**: Backend (firstName/lastName) → Frontend (name)
2. **Tạo module Staff**: Quản lý nhân viên riêng biệt
3. **Thêm menu Nhân viên**: Navigation và routing
4. **Tạo tài khoản test**: SQL script với 4 tài khoản mẫu

### 🎯 **KẾT QUẢ:**
- ✅ Hiển thị được danh sách Driver đã đăng ký
- ✅ Tạo được tài khoản Staff qua giao diện Admin
- ✅ Backend API hoạt động đúng với `/admin/users` và `/admin/staff`
- ✅ Frontend render dữ liệu đúng từ backend

## 📝 Ghi chú:

- Backend đã có sẵn API `/api/admin/staff` để tạo Staff
- Backend đã có sẵn API `/api/admin/users?role=CS_STAFF` để lấy danh sách Staff
- User entity có `firstName` và `lastName` riêng biệt, không có `fullName`
- User entity có `phone`, không có `phoneNumber`
- User entity không có field `active`, tất cả user đều active

## 🐛 Nếu vẫn gặp lỗi:

1. **Kiểm tra backend đang chạy**: `curl http://localhost:8080`
2. **Kiểm tra database có dữ liệu**: `SELECT * FROM nguoi_dung;`
3. **Kiểm tra token trong localStorage**: F12 → Application → Local Storage
4. **Xem console log**: F12 → Console để xem lỗi API
5. **Kiểm tra Network tab**: F12 → Network để xem response từ API

---

**Hoàn thành!** 🎉

Nếu cần thêm tính năng hoặc gặp vấn đề gì, hãy cho tôi biết!
