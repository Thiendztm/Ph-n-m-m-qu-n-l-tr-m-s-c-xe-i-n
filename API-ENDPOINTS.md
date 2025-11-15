# EV Charging Station - API Endpoints Documentation

## Base URL

```
http://localhost:8080/api
```

## Authentication

All authenticated endpoints require JWT token in header:

```
Authorization: Bearer {token}
```

---

## 1. DRIVER APIs (EV Driver Role)

### 1.1 Authentication & Profile

| Method | Endpoint          | Description                 | Auth Required |
| ------ | ----------------- | --------------------------- | ------------- |
| POST   | `/auth/register`  | Register new driver account | No            |
| POST   | `/auth/login`     | Login with email/phone      | No            |
| GET    | `/profile`        | Get driver profile info     | Yes           |
| PUT    | `/profile`        | Update driver profile       | Yes           |
| GET    | `/profile/wallet` | Get wallet balance          | Yes           |

### 1.2 Stations & Map

| Method | Endpoint                                           | Description               | Auth Required |
| ------ | -------------------------------------------------- | ------------------------- | ------------- |
| GET    | `/stations`                                        | Get all charging stations | No            |
| GET    | `/stations/{id}`                                   | Get station details       | No            |
| GET    | `/stations/nearby?lat={lat}&lng={lng}&radius={km}` | Get nearby stations       | No            |
| GET    | `/stations/{id}/chargers`                          | Get chargers at station   | No            |

### 1.3 Reservations

| Method | Endpoint             | Description            | Auth Required |
| ------ | -------------------- | ---------------------- | ------------- |
| POST   | `/reservations`      | Create new reservation | Yes           |
| GET    | `/reservations/my`   | Get my reservations    | Yes           |
| DELETE | `/reservations/{id}` | Cancel reservation     | Yes           |

### 1.4 Charging Sessions

| Method | Endpoint                            | Description                            | Auth Required |
| ------ | ----------------------------------- | -------------------------------------- | ------------- |
| POST   | `/charging/start`                   | Start charging session (after QR scan) | Yes           |
| GET    | `/charging/session/{sessionId}`     | Get session status/details             | Yes           |
| POST   | `/charging/stop`                    | Stop charging session                  | Yes           |
| GET    | `/charging/history?userId={userId}` | Get charging history                   | Yes           |
| WS     | `/ws/charging/{sessionId}`          | Real-time charging status updates      | Yes           |

### 1.5 Payment

| Method | Endpoint                       | Description              | Auth Required |
| ------ | ------------------------------ | ------------------------ | ------------- |
| POST   | `/payment/wallet/topup`        | Top up wallet            | Yes           |
| POST   | `/payment/charge`              | Pay for charging session | Yes           |
| GET    | `/payment/invoice/{sessionId}` | Get invoice/receipt      | Yes           |
| GET    | `/payment/history`             | Get payment history      | Yes           |

### 1.6 Analytics (Driver)

| Method | Endpoint                    | Description                  | Auth Required |
| ------ | --------------------------- | ---------------------------- | ------------- |
| GET    | `/driver/analytics/monthly` | Monthly charging cost report | Yes           |
| GET    | `/driver/analytics/habits`  | Charging habits statistics   | Yes           |

---

## 2. STAFF APIs (Charging Station Staff Role)

### 2.1 Station Monitoring

| Method | Endpoint                                | Description                  | Auth Required |
| ------ | --------------------------------------- | ---------------------------- | ------------- |
| GET    | `/staff/stations`                       | Get assigned stations        | Yes           |
| GET    | `/staff/stations/{stationId}/status`    | Get station real-time status | Yes           |
| GET    | `/staff/chargers/{chargerId}/status`    | Get charger status           | Yes           |
| GET    | `/staff/sessions/active?stationId={id}` | Get active charging sessions | Yes           |

### 2.2 Session Management

| Method | Endpoint                      | Description                       | Auth Required |
| ------ | ----------------------------- | --------------------------------- | ------------- |
| POST   | `/staff/sessions/start`       | Manually start session for driver | Yes           |
| POST   | `/staff/sessions/stop`        | Manually stop session             | Yes           |
| GET    | `/staff/sessions/{sessionId}` | Get session full details          | Yes           |

### 2.3 Payment at Station

| Method | Endpoint                                             | Description          | Auth Required |
| ------ | ---------------------------------------------------- | -------------------- | ------------- |
| POST   | `/staff/payments/cash`                               | Process cash payment | Yes           |
| GET    | `/staff/payments/history?stationId={id}&date={date}` | Get payment history  | Yes           |

### 2.4 Incident Reporting

| Method | Endpoint                                          | Description            | Auth Required |
| ------ | ------------------------------------------------- | ---------------------- | ------------- |
| POST   | `/staff/incidents`                                | Report new incident    | Yes           |
| GET    | `/staff/incidents?stationId={id}&status={status}` | Get incidents list     | Yes           |
| PUT    | `/staff/incidents/{incidentId}`                   | Update incident status | Yes           |

### 2.5 Charger Control

| Method | Endpoint                              | Description     | Auth Required |
| ------ | ------------------------------------- | --------------- | ------------- |
| POST   | `/staff/chargers/{chargerId}/enable`  | Enable charger  | Yes           |
| POST   | `/staff/chargers/{chargerId}/disable` | Disable charger | Yes           |

---

## 3. ADMIN APIs (System Administrator Role)

### 3.1 Users Management

| Method | Endpoint                                           | Description             | Auth Required |
| ------ | -------------------------------------------------- | ----------------------- | ------------- |
| GET    | `/admin/users?role={role}&page={page}&size={size}` | Get all users           | Yes           |
| GET    | `/admin/users/{userId}`                            | Get user details        | Yes           |
| POST   | `/admin/users`                                     | Create new user         | Yes           |
| PUT    | `/admin/users/{userId}`                            | Update user info        | Yes           |
| DELETE | `/admin/users/{userId}`                            | Delete user             | Yes           |
| POST   | `/admin/staff/assign`                              | Assign staff to station | Yes           |

### 3.2 Stations Management

| Method | Endpoint                          | Description        | Auth Required |
| ------ | --------------------------------- | ------------------ | ------------- |
| GET    | `/admin/stations?status={status}` | Get all stations   | Yes           |
| POST   | `/admin/stations`                 | Create new station | Yes           |
| PUT    | `/admin/stations/{stationId}`     | Update station     | Yes           |
| DELETE | `/admin/stations/{stationId}`     | Delete station     | Yes           |

### 3.3 Chargers Management

| Method | Endpoint                               | Description                 | Auth Required |
| ------ | -------------------------------------- | --------------------------- | ------------- |
| GET    | `/admin/stations/{stationId}/chargers` | Get chargers at station     | Yes           |
| POST   | `/admin/chargers`                      | Add new charger             | Yes           |
| PUT    | `/admin/chargers/{chargerId}`          | Update charger              | Yes           |
| DELETE | `/admin/chargers/{chargerId}`          | Delete charger              | Yes           |
| POST   | `/admin/chargers/{chargerId}/control`  | Remote control (start/stop) | Yes           |

### 3.4 Reports & Analytics

| Method | Endpoint                                                                            | Description         | Auth Required |
| ------ | ----------------------------------------------------------------------------------- | ------------------- | ------------- |
| GET    | `/admin/reports/revenue?startDate={date}&endDate={date}&groupBy={day\|week\|month}` | Revenue report      | Yes           |
| GET    | `/admin/reports/usage?startDate={date}&endDate={date}&stationId={id}`               | Usage statistics    | Yes           |
| GET    | `/admin/reports/peak-hours?period={week\|month}&stationId={id}`                     | Peak hours analysis | Yes           |
| GET    | `/admin/reports/station-performance/{stationId}?period={month\|year}`               | Station performance | Yes           |

### 3.5 Subscription Plans

| Method | Endpoint                | Description                | Auth Required |
| ------ | ----------------------- | -------------------------- | ------------- |
| GET    | `/admin/plans`          | Get all subscription plans | Yes           |
| POST   | `/admin/plans`          | Create new plan            | Yes           |
| PUT    | `/admin/plans/{planId}` | Update plan                | Yes           |
| DELETE | `/admin/plans/{planId}` | Delete plan                | Yes           |

### 3.6 Support & Incidents

| Method | Endpoint                                            | Description               | Auth Required |
| ------ | --------------------------------------------------- | ------------------------- | ------------- |
| GET    | `/admin/support/tickets?status={status}`            | Get support tickets       | Yes           |
| GET    | `/admin/support/tickets/{ticketId}`                 | Get ticket details        | Yes           |
| PUT    | `/admin/support/tickets/{ticketId}`                 | Update ticket status      | Yes           |
| GET    | `/admin/incidents?status={status}&severity={level}` | Get all incidents         | Yes           |
| POST   | `/admin/incidents/{incidentId}/assign`              | Assign incident to staff  | Yes           |
| POST   | `/admin/incidents/{incidentId}/resolve`             | Mark incident as resolved | Yes           |

---

## 4. WebSocket Endpoints (Real-time Updates)

### 4.1 Charging Status (Driver)

```
ws://localhost:8080/ws/charging/{sessionId}
```

**Purpose**: Real-time updates during charging (SOC%, kWh, time remaining, cost)

**Message Format**:

```json
{
  "sessionId": "string",
  "currentSOC": 75,
  "energyConsumed": 45.5,
  "estimatedTimeRemaining": 15,
  "currentCost": 159250,
  "status": "CHARGING|COMPLETED|ERROR"
}
```

### 4.2 Station Monitoring (Staff)

```
ws://localhost:8080/ws/staff/station/{stationId}
```

**Purpose**: Real-time station status updates for staff monitoring

**Message Format**:

```json
{
  "stationId": "string",
  "chargers": [
    {
      "chargerId": "string",
      "status": "AVAILABLE|CHARGING|OFFLINE|FAULTED",
      "currentSession": { "sessionId": "...", "driverId": "..." }
    }
  ],
  "activeSessions": 5,
  "totalRevenue": 5000000
}
```

### 4.3 Dashboard Updates (Admin)

```
ws://localhost:8080/ws/admin/dashboard
```

**Purpose**: Real-time dashboard metrics for admin

**Message Format**:

```json
{
  "totalRevenue": 150000000,
  "activeSessions": 45,
  "onlineChargers": 120,
  "offlineChargers": 5,
  "newIncidents": 2
}
```

---

## 5. Data Flow Between Actors

### 5.1 Charging Session Workflow

```
Driver → QR Scan → Start Session → Staff Monitor → Session Complete → Payment → History
```

**Step-by-step**:

1. **Driver**: Scans QR code → `POST /charging/start`
2. **Backend**: Creates session, returns `sessionId`
3. **Driver**: Connects to WebSocket → `ws://localhost:8080/ws/charging/{sessionId}`
4. **Staff**: Monitors via → `GET /staff/sessions/active?stationId={id}`
5. **Driver**: Stops session → `POST /charging/stop`
6. **Driver**: Pays → `POST /payment/charge`
7. **Both**: View history → Driver: `/charging/history`, Staff: `/staff/payments/history`

### 5.2 Incident Reporting Workflow

```
Staff → Report Incident → Admin View → Assign → Staff Resolve → Close
```

**Step-by-step**:

1. **Staff**: Reports → `POST /staff/incidents`
2. **Admin**: Views all → `GET /admin/incidents`
3. **Admin**: Assigns → `POST /admin/incidents/{id}/assign`
4. **Staff**: Updates → `PUT /staff/incidents/{id}`
5. **Admin**: Resolves → `POST /admin/incidents/{id}/resolve`

### 5.3 Payment Flow

```
Driver Charges → Online Payment OR Staff Cash Payment → Invoice → Reports
```

**Online Payment**:

- Driver → `POST /payment/charge` (wallet/banking)

**Cash Payment**:

- Staff → `POST /staff/payments/cash`

**Both flows** update:

- Driver's payment history
- Staff's payment logs
- Admin's revenue reports

---

## 6. Response Format Standards

### Success Response

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2025-11-15T10:30:00Z"
}
```

### Error Response

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error description",
    "details": { ... }
  },
  "timestamp": "2025-11-15T10:30:00Z"
}
```

### Pagination Response

```json
{
  "success": true,
  "data": [ ... ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

## 7. Status Codes

| Status        | Description             |
| ------------- | ----------------------- |
| `AVAILABLE`   | Charger ready to use    |
| `CHARGING`    | Active charging session |
| `RESERVED`    | Reserved by driver      |
| `OFFLINE`     | Charger not responding  |
| `FAULTED`     | Hardware error          |
| `MAINTENANCE` | Under maintenance       |

**Session Status**:

- `PENDING` - Created but not started
- `ACTIVE` - Currently charging
- `COMPLETED` - Successfully finished
- `STOPPED` - Manually stopped
- `FAILED` - Error occurred

**Payment Status**:

- `PENDING` - Awaiting payment
- `PROCESSING` - Payment in progress
- `COMPLETED` - Paid successfully
- `FAILED` - Payment failed
- `REFUNDED` - Money returned

**Incident Severity**:

- `LOW` - Minor issue
- `MEDIUM` - Moderate issue
- `HIGH` - Urgent issue
- `CRITICAL` - System down

---

## 8. Frontend Integration Guide

### Using the API Client (Recommended)

```javascript
import api from "./src/js/api-client.js";

// Driver: Get charging history
const history = await api.driver.getChargingHistory();

// Staff: Report incident
await api.staff.reportIncident(stationId, chargerId, "HARDWARE", "Description");

// Admin: Get revenue report
const revenue = await api.admin.getRevenueReport("2025-01-01", "2025-01-31");
```

### Real-time Updates

```javascript
import { RealtimeConnection } from "./src/js/api-client.js";

const conn = new RealtimeConnection(
  "/ws/charging/session123",
  (data) => console.log("Update:", data),
  (error) => console.error("Error:", error)
);

conn.connect();
```

---

**Last Updated**: November 15, 2025  
**Version**: 1.0  
**Maintained by**: Backend Team
