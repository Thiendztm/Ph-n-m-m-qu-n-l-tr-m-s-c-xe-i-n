"EV Charging Station Management System
Phần mềm quản lý trạm sạc xe điện"

Actors:
"EV Driver
CS Staff
Admin"

"1. Chức năng cho Tài xế (EV Driver)
a. Đăng ký & Quản lý tài khoản
+ Đăng ký/đăng nhập (qua email, số điện thoại, mạng xã hội, ...).
+ Quản lý hồ sơ: thông tin cá nhân, xe, lịch sử giao dịch.
b. Đặt chỗ & khởi động phiên sạc
+ Bản đồ trạm sạc theo vị trí, công suất, tình trạng trạm (trống/đang dùng), loại cổng sạc (CCS, CHAdeMO, AC), tốc độ sạc, giá cả.
+ Đặt trước điểm sạc.
+ Quét QR code / app để bắt đầu sạc.
+ Hiển thị trạng thái sạc (SOC %, thời gian còn lại, chi phí), nhận thông báo khi sạc đầy.
c. Thanh toán & ví điện tử
+ Thanh toán theo kWh, theo thời gian, hoặc gói thuê bao.
+ Thanh toán online (e-wallet, banking, ...).
+ Hóa đơn điện tử.
d. Lịch sử & phân tích cá nhân
+ Báo cáo chi phí sạc hằng tháng.
+ Thống kê thói quen sạc: thường sạc ở đâu, vào giờ nào, công suất bao nhiêu."
"2. Chức năng cho Nhân viên Trạm sạc (Charging Station Staff)
a. Thanh toán tại trạm sạc
+ Quản lý việc khởi động hoặc dừng phiên sạc.
+ Ghi nhận thanh toán tại chỗ phí sạc xe.
b. Theo dõi và báo cáo
+ Theo dõi tình trạng điểm sạc (online/offline, công suất, ...).
+ Báo cáo sự cố tại trạm sạc."
"3. Chức năng cho Quản trị (Admin)
a. Quản lý trạm & điểm sạc
+ Theo dõi tình trạng toàn bộ trạm sạc, điểm sạc (online/offline, công suất, ...).
+ Điều khiển từ xa: hoạt động/dừng.
b. Quản lý người dùng & gói dịch vụ
+ Quản lý khách hàng cá nhân/doanh nghiệp.
+ Tạo gói thuê bao: trả trước, trả sau, hội viên VIP.
+ Phân quyền nhân viên trạm sạc.
c. Báo cáo & thống kê
+ Theo dõi doanh thu theo trạm, khu vực, thời gian.
+ Báo cáo tần suất sử dụng trạm, giờ cao điểm.
+ AI gợi ý dự báo nhu cầu sử dụng trạm sạc để nâng cấp hạ tầng."

Front-end
client
│
├── user/                      # Người dùng (khách sạc xe)
│   ├── index.html             # Trang chủ, chọn gói, trạm sạc
│   ├── payment.html           # Trang thanh toán VNPay
│   ├── profile.html           # Trang cá nhân
│   └── src/
│       ├── js/
│       │   ├── user-common.js
│       │   ├── payment.js
│       │   └── map.js
│       ├── css/
│       │   ├── style.css
│       │   └── responsive.css
│       └── img/
│           └── logo.png
│
├── staff/                     # Nhân viên (quản lý trạm, giám sát sạc)
│   ├── dashboard.html         # Trang tổng quan
│   ├── manage-stations.html   # Quản lý trạm do nhân viên phụ trách
│   └── src/
│       ├── js/
│       │   ├── staff-common.js
│       │   ├── manage-stations.js
│       │   └── check-status.js
│       ├── css/
│       │   └── staff.css
│       └── img/
│           └── staff-icon.png
│
├── admin/                     # Quản trị viên cấp cao
│   ├── dashboard.html
│   ├── users.html             # Quản lý người dùng và nhân viên
│   ├── transactions.html      # Quản lý thanh toán
│   └── src/
│       ├── js/
│       │   ├── admin-common.js
│       │   ├── dashboard.js
│       │   └── manage-users.js
│       ├── css/
│       │   └── admin.css
│       └── img/
│           └── admin-logo.png
│
└── shared/                    # Thư mục dùng chung
    ├── js/
    │   ├── api.js             # Hàm gọi backend chung
    │   └── auth.js            # Quản lý login, token
    ├── css/
    │   └── variables.css
    └── components/
        ├── header.html
        ├── sidebar.html
        └── footer.html
