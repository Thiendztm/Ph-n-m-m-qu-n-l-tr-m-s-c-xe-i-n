# EV Charging Station - API Reference

## Overview

This document provides a comprehensive reference for all API endpoints required by the frontend application. Backend developers should implement these endpoints to ensure full system functionality.

---

## Authentication

All protected endpoints require JWT Bearer token authentication:

```
Authorization: Bearer <jwt_token>
```

### Auth Endpoints

#### POST /api/auth/register

Register a new user account.

**Request Body:**

```json
{
  "hoTen": "string",
  "email": "string",
  "sdt": "string",
  "matKhau": "string",
  "role": "DRIVER" | "CS_STAFF" | "ADMIN"
}
```

**Response:** `200 OK`

```json
{
  "userId": "string",
  "message": "Đăng ký thành công"
}
```

---

#### POST /api/auth/login

User login.

**Request Body:**

```json
{
  "email": "string",
  "matKhau": "string"
}
```

**Response:** `200 OK`

```json
{
  "accessToken": "string",
  "userId": "string",
  "role": "string",
  "hoTen": "string"
}
```

---

## User Profile

#### GET /api/profile

Get current user profile information.

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`

```json
{
  "hoTen": "string",
  "email": "string",
  "sdt": "string",
  "diaChi": "string"
}
```

---

#### GET /api/profile/wallet

Get user wallet balance.

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`

```json
{
  "balance": 0
}
```

---

#### GET /api/profile/vehicle

Get user vehicle information.

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`

```json
{
  "licensePlate": "string",
  "model": "string",
  "connectorType": "CCS2" | "CHAdeMO" | "Type2" | "Type1",
  "batteryCapacity": 0
}
```

**Response:** `404 Not Found` (if no vehicle data exists)

---

#### POST /api/profile/vehicle

Create or update vehicle information.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**

```json
{
  "licensePlate": "string",
  "model": "string",
  "connectorType": "CCS2" | "CHAdeMO" | "Type2" | "Type1",
  "batteryCapacity": 0
}
```

**Response:** `200 OK`

```json
{
  "message": "Vehicle data saved successfully"
}
```

---

## Charging Stations (Driver)

#### GET /api/stations

Get all charging stations with their chargers.

**Query Parameters:**

- `status` (optional): Filter by station status (`ACTIVE`, `INACTIVE`)

**Response:** `200 OK`

```json
[
  {
    "stationId": "string",
    "tenTram": "string",
    "diaChi": "string",
    "viDo": 0,
    "kinhDo": 0,
    "soDienThoai": "string",
    "trangThai": "ACTIVE" | "INACTIVE",
    "chargers": [
      {
        "chargerId": "string",
        "loaiCongSac": "CCS2" | "CHAdeMO" | "Type2" | "Type1",
        "congSuat": 0,
        "trangThai": "AVAILABLE" | "CHARGING" | "OFFLINE" | "MAINTENANCE",
        "giaMotKwh": 0
      }
    ]
  }
]
```

---

#### GET /api/stations/{id}

Get station details by ID.

**Response:** `200 OK` (Same structure as single station in GET /api/stations)

---

## Charging Sessions (Driver)

#### GET /api/charging/history

Get user's charging history.

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**

- `userId` (optional): User ID to fetch history for

**Response:** `200 OK`

```json
{
  "sessions": [
    {
      "sessionId": "string",
      "stationName": "string",
      "startTime": "ISO8601 datetime",
      "endTime": "ISO8601 datetime",
      "energyConsumed": 0,
      "totalCost": 0,
      "startSoc": 0,
      "endSoc": 0,
      "status": "COMPLETED" | "FAILED" | "IN_PROGRESS"
    }
  ]
}
```

**Alternative Response:** `200 OK` (Array format)

```json
[
  {
    "sessionId": "string",
    "stationName": "string",
    "startTime": "ISO8601 datetime",
    "endTime": "ISO8601 datetime",
    "energyConsumed": 0,
    "totalCost": 0,
    "startSoc": 0,
    "endSoc": 0,
    "status": "COMPLETED" | "FAILED" | "IN_PROGRESS"
  }
]
```

---

#### GET /api/charging/session/{sessionId}

Get detailed information about a specific charging session.

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`

```json
{
  "sessionId": "string",
  "stationName": "string",
  "stationAddress": "string",
  "startTime": "ISO8601 datetime",
  "endTime": "ISO8601 datetime",
  "startSoc": 0,
  "endSoc": 0,
  "energyConsumed": 0,
  "powerOutput": 0,
  "costPerKwh": 0,
  "totalCost": 0,
  "paymentMethod": "string",
  "status": "COMPLETED" | "FAILED" | "IN_PROGRESS"
}
```

**Response:** `404 Not Found` (if session doesn't exist)

---

## Staff APIs

#### GET /api/staff/station/{id}/status

Get real-time status of a charging station.

**Headers:** `Authorization: Bearer <token>` (Staff role required)

**Response:** `200 OK`

```json
{
  "stationId": "string",
  "tenTram": "string",
  "diaChi": "string",
  "trangThai": "ACTIVE" | "INACTIVE",
  "chargers": [
    {
      "chargerId": "string",
      "loaiCongSac": "string",
      "congSuat": 0,
      "trangThai": "AVAILABLE" | "CHARGING" | "OFFLINE" | "MAINTENANCE",
      "currentSession": {
        "sessionId": "string",
        "userName": "string",
        "startTime": "ISO8601 datetime",
        "energyConsumed": 0,
        "currentSoc": 0,
        "estimatedEndTime": "ISO8601 datetime"
      } | null
    }
  ]
}
```

---

#### POST /api/staff/sessions/start

Start a charging session manually.

**Headers:** `Authorization: Bearer <token>` (Staff role required)

**Request Body:**

```json
{
  "chargerId": "string",
  "userId": "string",
  "startSoc": 0
}
```

**Response:** `200 OK`

```json
{
  "sessionId": "string",
  "message": "Session started successfully"
}
```

---

#### POST /api/staff/sessions/{id}/stop

Stop a charging session.

**Headers:** `Authorization: Bearer <token>` (Staff role required)

**Request Body:**

```json
{
  "endSoc": 0,
  "reason": "string" (optional)
}
```

**Response:** `200 OK`

```json
{
  "message": "Session stopped successfully",
  "sessionId": "string",
  "energyConsumed": 0,
  "totalCost": 0
}
```

---

#### POST /api/staff/payments/confirm

Confirm a payment transaction.

**Headers:** `Authorization: Bearer <token>` (Staff role required)

**Request Body:**

```json
{
  "sessionId": "string",
  "amount": 0,
  "method": "CASH" | "CARD" | "WALLET"
}
```

**Response:** `200 OK`

```json
{
  "message": "Payment confirmed",
  "transactionId": "string"
}
```

---

## Admin APIs

### Station Management

#### GET /api/admin/stations

Get all stations for management.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Response:** `200 OK`

```json
[
  {
    "stationId": "string",
    "tenTram": "string",
    "diaChi": "string",
    "viDo": 0,
    "kinhDo": 0,
    "soDienThoai": "string",
    "trangThai": "ACTIVE" | "INACTIVE",
    "chargerCount": 0,
    "activeChargers": 0
  }
]
```

---

#### GET /api/admin/stations/{id}

Get station details with chargers.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Response:** `200 OK`

```json
{
  "stationId": "string",
  "tenTram": "string",
  "diaChi": "string",
  "viDo": 0,
  "kinhDo": 0,
  "soDienThoai": "string",
  "trangThai": "ACTIVE" | "INACTIVE",
  "chargers": [
    {
      "chargerId": "string",
      "loaiCongSac": "string",
      "congSuat": 0,
      "trangThai": "AVAILABLE" | "CHARGING" | "OFFLINE" | "MAINTENANCE",
      "giaMotKwh": 0
    }
  ]
}
```

---

#### POST /api/admin/stations

Create a new charging station.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Request Body:**

```json
{
  "tenTram": "string",
  "diaChi": "string",
  "viDo": 0,
  "kinhDo": 0,
  "soDienThoai": "string",
  "trangThai": "ACTIVE" | "INACTIVE"
}
```

**Response:** `201 Created`

```json
{
  "stationId": "string",
  "message": "Station created successfully"
}
```

---

#### PUT /api/admin/stations/{id}

Update station information.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Request Body:** (Same as POST /api/admin/stations)

**Response:** `200 OK`

```json
{
  "message": "Station updated successfully"
}
```

---

#### DELETE /api/admin/stations/{id}

Delete a charging station.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Response:** `200 OK`

```json
{
  "message": "Station deleted successfully"
}
```

---

### Charger Management

#### POST /api/admin/chargers

Create a new charger for a station.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Request Body:**

```json
{
  "stationId": "string",
  "loaiCongSac": "CCS2" | "CHAdeMO" | "Type2" | "Type1",
  "congSuat": 0,
  "trangThai": "AVAILABLE" | "CHARGING" | "OFFLINE" | "MAINTENANCE",
  "giaMotKwh": 0
}
```

**Response:** `201 Created`

```json
{
  "chargerId": "string",
  "message": "Charger created successfully"
}
```

---

#### PUT /api/admin/chargers/{id}

Update charger information.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Request Body:**

```json
{
  "loaiCongSac": "CCS2" | "CHAdeMO" | "Type2" | "Type1",
  "congSuat": 0,
  "trangThai": "AVAILABLE" | "CHARGING" | "OFFLINE" | "MAINTENANCE",
  "giaMotKwh": 0
}
```

**Response:** `200 OK`

```json
{
  "message": "Charger updated successfully"
}
```

---

#### DELETE /api/admin/chargers/{id}

Delete a charger.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Response:** `200 OK`

```json
{
  "message": "Charger deleted successfully"
}
```

---

### User Management

#### GET /api/admin/users

Get all users.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Query Parameters:**

- `role` (optional): Filter by role (`DRIVER`, `CS_STAFF`, `ADMIN`)
- `active` (optional): Filter by active status (`true`, `false`)

**Response:** `200 OK`

```json
[
  {
    "userId": "string",
    "hoTen": "string",
    "email": "string",
    "sdt": "string",
    "role": "DRIVER" | "CS_STAFF" | "ADMIN",
    "active": true,
    "createdAt": "ISO8601 datetime"
  }
]
```

---

#### POST /api/admin/users

Create a new user.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Request Body:**

```json
{
  "hoTen": "string",
  "email": "string",
  "sdt": "string",
  "matKhau": "string",
  "role": "DRIVER" | "CS_STAFF" | "ADMIN",
  "active": true
}
```

**Response:** `201 Created`

```json
{
  "userId": "string",
  "message": "User created successfully"
}
```

---

#### PUT /api/admin/users/{id}

Update user information.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Request Body:**

```json
{
  "hoTen": "string",
  "email": "string",
  "sdt": "string",
  "role": "DRIVER" | "CS_STAFF" | "ADMIN",
  "active": true
}
```

**Response:** `200 OK`

```json
{
  "message": "User updated successfully"
}
```

---

#### DELETE /api/admin/users/{id}

Delete a user account.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Response:** `200 OK`

```json
{
  "message": "User deleted successfully"
}
```

---

### Reports & Analytics

#### GET /api/admin/reports/summary

Get dashboard summary statistics.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Query Parameters:**

- `days` (required): Number of days to include (7, 30, 90, 365)

**Response:** `200 OK`

```json
{
  "totalRevenue": 0,
  "revenueChange": 0,
  "totalEnergy": 0,
  "energyChange": 0,
  "totalSessions": 0,
  "sessionsChange": 0,
  "newUsers": 0,
  "usersChange": 0
}
```

---

#### GET /api/admin/reports/revenue

Get revenue chart data.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Query Parameters:**

- `days` (required): Number of days (7, 30, 90, 365)

**Response:** `200 OK`

```json
{
  "labels": ["2024-01-01", "2024-01-02", ...],
  "values": [150000, 200000, ...]
}
```

---

#### GET /api/admin/reports/energy

Get energy consumption chart data.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Query Parameters:**

- `days` (required): Number of days

**Response:** `200 OK`

```json
{
  "labels": ["2024-01-01", "2024-01-02", ...],
  "values": [450.5, 523.2, ...]
}
```

---

#### GET /api/admin/reports/user-growth

Get user growth chart data.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Query Parameters:**

- `days` (required): Number of days

**Response:** `200 OK`

```json
{
  "labels": ["2024-01-01", "2024-01-02", ...],
  "values": [5, 8, 12, ...]
}
```

---

#### GET /api/admin/reports/top-stations

Get top performing stations.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Query Parameters:**

- `days` (required): Number of days
- `limit` (optional): Number of stations to return (default: 5)

**Response:** `200 OK`

```json
[
  {
    "stationName": "string",
    "revenue": 0,
    "sessionCount": 0
  }
]
```

---

#### GET /api/admin/reports/recent-sessions

Get recent charging sessions.

**Headers:** `Authorization: Bearer <token>` (Admin role required)

**Query Parameters:**

- `limit` (optional): Number of sessions to return (default: 10)

**Response:** `200 OK`

```json
[
  {
    "sessionId": "string",
    "stationName": "string",
    "userName": "string",
    "startTime": "ISO8601 datetime",
    "energyConsumed": 0,
    "totalCost": 0,
    "status": "COMPLETED" | "FAILED" | "IN_PROGRESS"
  }
]
```

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request

```json
{
  "error": "Bad Request",
  "message": "Detailed error message"
}
```

### 401 Unauthorized

```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden

```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

### 404 Not Found

```json
{
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error

```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Notes for Backend Developers

1. **Authentication**: All protected endpoints must validate JWT tokens and check user roles.

2. **CORS**: Enable CORS for frontend origins during development.

3. **Validation**: Implement request validation for all input data.

4. **Vietnamese Characters**: Ensure UTF-8 encoding for Vietnamese text fields.

5. **Date Format**: Use ISO8601 format for all datetime fields.

6. **Pagination**: Consider adding pagination for large datasets (history, users, etc.).

7. **Real-time Updates**: Consider WebSocket integration for:

   - Staff dashboard (charger status updates)
   - Active charging sessions
   - Payment confirmations

8. **File Upload**: Consider adding avatar upload endpoint for user profiles.

9. **Search**: Add search functionality to user and station management endpoints.

10. **Audit Logs**: Consider logging all admin actions for security purposes.

---

## Testing

Use the following test accounts:

**Admin:**

- Email: `admin@ev.com`
- Password: `admin123`

**Staff:**

- Email: `staff@ev.com`
- Password: `staff123`

**Driver:**

- Email: `driver@ev.com`
- Password: `driver123`

---

## Version History

- **v1.0** (2024-01-XX): Initial API specification
- All endpoints documented based on frontend implementation
- Ready for backend development
