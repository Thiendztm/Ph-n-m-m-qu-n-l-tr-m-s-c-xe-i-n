# EV Charging Station Management System

## Phần mềm quản lý trạm sạc xe điện

Hệ thống quản lý trạm sạc xe điện được phát triển bằng Java với Hibernate và SQL Server.

## Actors (Vai trò người dùng)
- **EV Driver**: Tài xế xe điện
- **CS Staff**: Nhân viên trạm sạc 
- **Admin**: Quản trị viên hệ thống

## Chức năng chính

### 1. Chức năng cho Tài xế (EV Driver)
- **Đăng ký & Quản lý tài khoản**: Đăng ký/đăng nhập, quản lý hồ sơ cá nhân và thông tin xe
- **Đặt chỗ & khởi động phiên sạc**: Tìm trạm sạc, đặt trước điểm sạc, quét QR code để bắt đầu sạc
- **Thanh toán & ví điện tử**: Thanh toán theo kWh, ví điện tử, hóa đơn điện tử
- **Lịch sử & phân tích cá nhân**: Báo cáo chi phí, thống kê thói quen sạc

### 2. Chức năng cho Nhân viên Trạm sạc (CS Staff)
- **Thanh toán tại trạm sạc**: Quản lý khởi động/dừng phiên sạc, thanh toán tiền mặt
- **Theo dõi và báo cáo**: Theo dõi tình trạng điểm sạc, báo cáo sự cố

### 3. Chức năng cho Quản trị (Admin)
- **Quản lý trạm & điểm sạc**: Theo dõi tình trạng toàn bộ hệ thống, điều khiển từ xa
- **Quản lý người dùng & gói dịch vụ**: Quản lý khách hàng, tạo gói thuê bao, phân quyền
- **Báo cáo & thống kê**: Theo dõi doanh thu, báo cáo tần suất sử dụng, phân tích dữ liệu

## Cấu trúc Database

### Các bảng chính:
- **users**: Thông tin người dùng (tài xế, nhân viên, admin)
- **charging_stations**: Thông tin trạm sạc
- **charging_points**: Điểm sạc tại các trạm
- **bookings**: Đặt chỗ sạc
- **charging_sessions**: Phiên sạc
- **payments**: Thanh toán
- **service_packages**: Gói dịch vụ (trả trước, trả sau, VIP)
- **user_package_subscriptions**: Đăng ký gói dịch vụ của người dùng
- **incidents**: Báo cáo sự cố tại trạm sạc

## Cài đặt và Chạy ứng dụng

### Yêu cầu hệ thống:
- Java 11 hoặc cao hơn
- SQL Server
- Maven

### Cài đặt:

1. **Cấu hình Database**:
   - Tạo database `EVDATABASE` trong SQL Server
   - Cập nhật thông tin kết nối trong `src/main/resources/hibernate.cfg.xml`:
     ```xml
     <property name="connection.url">jdbc:sqlserver://localhost:1433;DatabaseName=EVDATABASE;encrypt=true;trustServerCertificate=true</property>
     <property name="connection.username">sa</property>
     <property name="connection.password">YOUR_PASSWORD</property>
     ```

2. **Build và chạy ứng dụng**:
   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass="uth.edu.vn.Main"
   ```

3. **Hoặc chạy từ IDE**:
   - Import project vào IDE (IntelliJ IDEA, Eclipse)
   - Chạy class `uth.edu.vn.Main`

### Dữ liệu mẫu:
Khi chạy lần đầu, hệ thống sẽ tự động:
- Tạo tất cả các bảng trong database
- Tạo dữ liệu mẫu bao gồm:
  - 2 trạm sạc với các điểm sạc khác nhau
  - Tài khoản admin, nhân viên và tài xế mẫu
  - 3 gói dịch vụ (Basic Prepaid, Premium Monthly, VIP Unlimited)
  - Dữ liệu demo cho testing

## Cách sử dụng

### Login thông tin mẫu:
**EV Drivers:**
- Email: `driver1@email.com`, Password: `pass123`
- Email: `driver2@email.com`, Password: `pass456`

**Staff:**
- Email: `staff1@evcs.com`, Password: `staff123`
- Email: `staff2@evcs.com`, Password: `staff456`

### Menu chính:
1. **EV Driver Functions**: Các chức năng dành cho tài xế
2. **Charging Station Staff Functions**: Các chức năng dành cho nhân viên
3. **Admin Functions**: Các chức năng dành cho quản trị viên
4. **View System Overview**: Xem tổng quan hệ thống

## Cấu trúc mã nguồn

```
src/main/java/uth/edu/vn/
├── entity/           # JPA Entities
│   ├── User.java
│   ├── ChargingStation.java
│   ├── ChargingPoint.java
│   ├── Booking.java
│   ├── ChargingSession.java
│   ├── Payment.java
│   └── [Enums]
├── service/          # Business Logic
│   ├── EVDriverService.java
│   ├── CSStaffService.java
│   └── AdminService.java
├── util/            # Utilities
│   └── HibernateUtil.java
└── Main.java        # Main Application
```

## Tính năng kỹ thuật

- **ORM**: Hibernate/JPA cho quản lý database
- **Database**: SQL Server với auto-schema generation
- **Architecture**: Service Layer Pattern
- **Connection Pooling**: Hibernate built-in connection pool
- **Transaction Management**: Hibernate Transaction API

## Development Notes

- Sử dụng `hbm2ddl.auto=create-drop` để tự động tạo/xóa schema (chỉ dành cho development)
- Để production, thay đổi thành `update` hoặc `validate`
- SQL queries được log ra console để debug (`show_sql=true`)

## Demo Features

Ứng dụng bao gồm tất cả chức năng chính như mô tả trong đề tài:
- ✅ Quản lý người dùng 3 vai trò
- ✅ Quản lý trạm sạc và điểm sạc
- ✅ Đặt chỗ và quản lý phiên sạc
- ✅ Hệ thống thanh toán và ví điện tử
- ✅ Báo cáo và thống kê
- ✅ Auto-generate database schema và sample data