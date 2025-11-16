# EV Charging Station - Deployment Guide

## Overview

This guide provides step-by-step instructions for deploying and testing the EV Charging Station system.

---

## System Architecture

```
┌─────────────────┐
│   Frontend      │
│   (HTML/CSS/JS) │
│   Port: 8080    │
└────────┬────────┘
         │
         │ REST API
         │ JWT Auth
         ▼
┌─────────────────┐
│   Spring Boot   │
│   Backend API   │
│   Port: 8080    │
└────────┬────────┘
         │
         │ JPA/Hibernate
         ▼
┌─────────────────┐
│   PostgreSQL    │
│   Database      │
│   Port: 5432    │
└─────────────────┘
```

---

## Prerequisites

### Required Software

- **JDK 17 or higher**
- **Maven 3.8+**
- **PostgreSQL 14+**
- **Node.js 16+** (for development only)
- **Git**

### Optional Tools

- **Postman** (for API testing)
- **pgAdmin** (for database management)
- **VS Code** (recommended IDE)

---

## Database Setup

### 1. Create Database

```sql
CREATE DATABASE ev_charging_station;
CREATE USER ev_user WITH ENCRYPTED PASSWORD 'ev_password';
GRANT ALL PRIVILEGES ON DATABASE ev_charging_station TO ev_user;
```

### 2. Configure application.properties

Edit `ev/src/main/resources/application.properties`:

```properties
spring.application.name=ev

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/ev_charging_station
spring.datasource.username=ev_user
spring.datasource.password=ev_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration
jwt.secret=your-secret-key-here-change-this-in-production
jwt.expiration=86400000

# Server Configuration
server.port=8080

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 3. Database Schema

The application will auto-generate tables using JPA annotations. Key entities:

- `users` - User accounts
- `stations` - Charging stations
- `chargers` - Charging points
- `sessions` - Charging sessions
- `vehicles` - User vehicles
- `transactions` - Payment transactions

---

## Backend Deployment

### 1. Build the Project

```cmd
cd ev
mvn clean package -DskipTests
```

This creates: `target/ev-0.0.1-SNAPSHOT.jar`

### 2. Run the Application

**Option A: Using Maven**

```cmd
mvn spring-boot:run
```

**Option B: Using JAR file**

```cmd
java -jar target/ev-0.0.1-SNAPSHOT.jar
```

### 3. Verify Backend is Running

Open browser: `http://localhost:8080`

Expected: You should see the login page

---

## Frontend Deployment

### 1. Copy Frontend Files

The frontend files are already copied to Spring Boot's static folder via `copy-frontend.bat`.

To manually copy:

```cmd
copy-frontend.bat
```

This copies all files from `front-end/client/` to `ev/src/main/resources/static/`

### 2. Frontend Structure

```
static/
├── index.html           # Driver home page (map view)
├── login.html           # Login page
├── register.html        # Registration page
├── profile.html         # User profile
├── charging-status.html # Active session monitoring
├── payment.html         # Payment page
├── analytics.html       # Driver analytics
├── src/
│   ├── css/            # Stylesheets
│   ├── js/             # JavaScript modules
│   └── img/            # Images
├── admin/              # Admin dashboard
│   ├── index.html
│   ├── login.html
│   ├── css/
│   └── js/
└── staff/              # Staff dashboard
    ├── index.html
    ├── login.html
    ├── css/
    └── js/
```

---

## Testing Guide

### 1. Create Test Users

**Using SQL:**

```sql
-- Admin user
INSERT INTO users (ho_ten, email, sdt, mat_khau, role, active)
VALUES ('Admin User', 'admin@ev.com', '0900000001', '$2a$10$hashedpassword', 'ADMIN', true);

-- Staff user
INSERT INTO users (ho_ten, email, sdt, mat_khau, role, active)
VALUES ('Staff User', 'staff@ev.com', '0900000002', '$2a$10$hashedpassword', 'CS_STAFF', true);

-- Driver user
INSERT INTO users (ho_ten, email, sdt, mat_khau, role, active)
VALUES ('Driver User', 'driver@ev.com', '0900000003', '$2a$10$hashedpassword', 'DRIVER', true);
```

**Note:** Replace `$2a$10$hashedpassword` with actual BCrypt hashed passwords.

### 2. Create Test Stations

```sql
INSERT INTO stations (ten_tram, dia_chi, vi_do, kinh_do, so_dien_thoai, trang_thai)
VALUES
  ('Trạm Nguyễn Huệ', '123 Nguyễn Huệ, Q1, TP.HCM', 10.7769, 106.7009, '0900111111', 'ACTIVE'),
  ('Trạm Lê Lợi', '456 Lê Lợi, Q1, TP.HCM', 10.7750, 106.7050, '0900222222', 'ACTIVE');
```

### 3. Create Test Chargers

```sql
INSERT INTO chargers (station_id, loai_cong_sac, cong_suat, trang_thai, gia_mot_kwh)
VALUES
  (1, 'CCS2', 50, 'AVAILABLE', 3500),
  (1, 'CHAdeMO', 50, 'AVAILABLE', 3500),
  (2, 'Type2', 22, 'AVAILABLE', 3000);
```

---

## End-to-End Testing

### Test Scenario 1: Driver Flow

1. **Registration**

   - Go to: `http://localhost:8080/register.html`
   - Fill in: Name, Email, Phone, Password
   - Select Role: "Tài xế" (Driver)
   - Submit and verify success message

2. **Login**

   - Go to: `http://localhost:8080/login.html`
   - Enter registered email and password
   - Submit and verify redirect to map page

3. **View Stations**

   - On map page, verify stations appear as markers
   - Click a station marker to see details
   - Check charger availability and pricing

4. **Profile Management**

   - Go to: Profile page (navbar icon)
   - Click "Chỉnh sửa"
   - Update vehicle information:
     - License Plate: `59A-12345`
     - Model: `Tesla Model 3`
     - Connector Type: `CCS2`
     - Battery Capacity: `75`
   - Save and verify display updates

5. **Charging History**
   - Go to: Charging history page
   - Verify empty state shows "Chưa có lịch sử sạc"
   - After completing a charge, verify session appears

---

### Test Scenario 2: Staff Flow

1. **Staff Login**

   - Go to: `http://localhost:8080/staff/login.html`
   - Login with staff credentials
   - Verify redirect to staff dashboard

2. **Select Station**

   - Dashboard shows list of stations
   - Click a station to view chargers
   - Verify charger status indicators

3. **Manage Charging Session**

   - Click "Bắt đầu sạc" on an available charger
   - Enter user ID and starting SOC
   - Submit and verify session starts
   - Monitor session in "Phiên sạc" tab

4. **Stop Session**

   - In sessions list, click "Dừng sạc"
   - Enter ending SOC
   - Verify session stops and cost calculated

5. **Confirm Payment**
   - Go to "Thanh toán" tab
   - View pending payments
   - Click "Xác nhận" to confirm payment
   - Verify payment status updates

---

### Test Scenario 3: Admin Flow

1. **Admin Login**

   - Go to: `http://localhost:8080/admin/login.html`
   - Login with admin credentials
   - Verify redirect to admin dashboard

2. **Manage Stations**

   - Click "Quản lý trạm sạc" in sidebar
   - Test creating new station:
     - Click "Thêm trạm sạc"
     - Fill in all fields
     - Submit and verify station appears
   - Test editing station:
     - Click edit icon on a station
     - Modify fields
     - Save and verify changes
   - Test deleting station:
     - Click delete icon
     - Confirm deletion
     - Verify station removed

3. **Manage Chargers**

   - Select a station
   - Click "Thêm máy sạc"
   - Fill in charger details
   - Submit and verify charger added
   - Test edit and delete operations

4. **Manage Users**

   - Click "Quản lý người dùng"
   - Test filtering by role (Tất cả, Tài xế, Nhân viên, Admin)
   - Test search functionality
   - Test creating new user:
     - Click "Thêm người dùng"
     - Fill in user details
     - Submit and verify user appears
   - Test editing user (change role, active status)
   - Test deleting user

5. **View Reports**
   - Click "Báo cáo & Phân tích"
   - Verify 4 stat cards display correctly
   - Verify 3 charts render (Revenue, Energy, User Growth)
   - Test time range filters (7/30/90/365 days)
   - Test CSV export for revenue and energy
   - Check top stations list
   - Check recent sessions table

---

## API Testing with Postman

### 1. Setup

Create a Postman collection with environment variables:

- `baseUrl`: `http://localhost:8080/api`
- `token`: (will be set after login)

### 2. Test Authentication

**Register:**

```
POST {{baseUrl}}/auth/register
Body (JSON):
{
  "hoTen": "Test User",
  "email": "test@example.com",
  "sdt": "0900000000",
  "matKhau": "password123",
  "role": "DRIVER"
}
```

**Login:**

```
POST {{baseUrl}}/auth/login
Body (JSON):
{
  "email": "test@example.com",
  "matKhau": "password123"
}
```

Save the `accessToken` from response to environment variable.

### 3. Test Protected Endpoints

**Get Profile:**

```
GET {{baseUrl}}/profile
Headers:
  Authorization: Bearer {{token}}
```

**Get Stations:**

```
GET {{baseUrl}}/stations
```

**Get Charging History:**

```
GET {{baseUrl}}/charging/history
Headers:
  Authorization: Bearer {{token}}
```

---

## Common Issues and Solutions

### Issue 1: Backend won't start

**Error:** `Port 8080 is already in use`

**Solution:**

```cmd
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /F /PID <PID>
```

---

### Issue 2: Database connection failed

**Error:** `Connection refused` or `Authentication failed`

**Solution:**

1. Verify PostgreSQL is running: `pg_isready`
2. Check database exists: `psql -U postgres -l`
3. Verify credentials in `application.properties`
4. Check PostgreSQL is listening on port 5432

---

### Issue 3: Frontend not loading

**Error:** `404 Not Found` for HTML pages

**Solution:**

1. Run `copy-frontend.bat` to copy files
2. Rebuild project: `mvn clean package`
3. Verify files exist in `ev/src/main/resources/static/`
4. Restart Spring Boot application

---

### Issue 4: API returns 401 Unauthorized

**Error:** `Invalid or expired token`

**Solution:**

1. Check token is included in `Authorization` header
2. Verify token format: `Bearer <token>`
3. Check JWT secret matches in application.properties
4. Token may be expired - login again

---

### Issue 5: CORS errors in browser

**Error:** `CORS policy: No 'Access-Control-Allow-Origin' header`

**Solution:**
Add CORS configuration to Spring Boot:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
```

---

## Performance Optimization

### 1. Database Indexing

Add indexes for frequently queried columns:

```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_status ON sessions(status);
CREATE INDEX idx_chargers_station_id ON chargers(station_id);
CREATE INDEX idx_stations_status ON stations(trang_thai);
```

### 2. Caching

Consider adding Spring Cache for:

- Station list
- User profiles
- Reports data

### 3. Connection Pooling

Configure HikariCP in `application.properties`:

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

---

## Security Checklist

- [ ] Change JWT secret in production
- [ ] Use HTTPS for all communication
- [ ] Implement rate limiting for API endpoints
- [ ] Add CSRF protection
- [ ] Sanitize all user inputs
- [ ] Hash passwords with BCrypt (strength 10+)
- [ ] Validate file uploads
- [ ] Set secure cookie flags
- [ ] Enable SQL injection prevention (JPA handles this)
- [ ] Add audit logging for admin actions

---

## Monitoring and Logging

### 1. Application Logs

Logs are saved to: `logs/spring-boot-application.log`

Configure in `application.properties`:

```properties
logging.level.root=INFO
logging.level.uth.edu.vn=DEBUG
logging.file.name=logs/spring-boot-application.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

### 2. Database Queries

Enable SQL logging for debugging:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

## Backup and Recovery

### Database Backup

**Create backup:**

```bash
pg_dump -U ev_user -d ev_charging_station > backup_$(date +%Y%m%d).sql
```

**Restore backup:**

```bash
psql -U ev_user -d ev_charging_station < backup_20240101.sql
```

### Application Backup

Backup these directories:

- `ev/src/main/resources/application.properties`
- `ev/src/main/resources/static/` (frontend files)
- Database dumps
- Environment configuration files

---

## Production Deployment Checklist

- [ ] Update `application.properties` for production database
- [ ] Change JWT secret to strong random value
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (not `update`)
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Set up database backups (daily)
- [ ] Configure monitoring (Prometheus, Grafana)
- [ ] Set up log aggregation (ELK stack)
- [ ] Configure CDN for static assets
- [ ] Set up load balancer (if needed)
- [ ] Configure auto-scaling
- [ ] Document environment variables
- [ ] Create deployment pipeline (CI/CD)
- [ ] Perform security audit
- [ ] Load testing
- [ ] Create disaster recovery plan

---

## Support and Maintenance

### Regular Tasks

**Daily:**

- Monitor application logs for errors
- Check database connections
- Verify backup completion

**Weekly:**

- Review user reports
- Check API performance metrics
- Update dependencies if needed

**Monthly:**

- Database optimization (VACUUM, ANALYZE)
- Security patch updates
- Review access logs

### Troubleshooting Commands

```cmd
# Check application status
curl http://localhost:8080/actuator/health

# View recent logs
tail -f logs/spring-boot-application.log

# Check database connections
psql -U ev_user -d ev_charging_station -c "SELECT count(*) FROM pg_stat_activity;"

# Monitor JVM memory
jcmd <PID> GC.heap_info
```

---

## Contact

For technical support or questions:

- Email: support@ev-charging-station.com
- Documentation: See `API-REFERENCE.md`
- Integration Guide: See `INTEGRATION-GUIDE.md`

---

**Version:** 1.0  
**Last Updated:** 2024  
**Status:** Production Ready
