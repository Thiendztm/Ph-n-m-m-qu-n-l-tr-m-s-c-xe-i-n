# 🔧 SỬA LỖI: API_BASE_URL Conflict

## ❌ Lỗi gốc:

```
Uncaught SyntaxError: Identifier 'API_BASE_URL' has already been declared (at map.js:1:1)
```

## 🔍 Nguyên nhân:

- **navbar.js** load trước và khai báo: `const API_BASE_URL = '...'`
- **map.js** load sau và cũng khai báo: `const API_BASE_URL = '...'`
- → Conflict trong global scope!

## ✅ Giải pháp:

### 1. **navbar.js** (dòng 1-4):

```javascript
// BEFORE:
const API_BASE_URL = "http://localhost:8080/api";

// AFTER:
window.API_BASE_URL = window.API_BASE_URL || "http://localhost:8080/api";
const API_BASE_URL = window.API_BASE_URL;
```

### 2. **map.js** (dòng 41-44):

```javascript
// BEFORE:
const API_BASE_URL = "http://localhost:8080/api";

// AFTER:
if (typeof API_BASE_URL === "undefined") {
  var API_BASE_URL = "http://localhost:8080/api";
}
```

## 🎯 Kết quả:

- ✅ `window.API_BASE_URL` được khai báo 1 lần duy nhất
- ✅ Các file khác có thể dùng lại mà không conflict
- ✅ Backward compatible với code cũ

## 🧪 Test:

```
1. Refresh browser: Ctrl + Shift + R
2. Mở Console (F12) → Không còn SyntaxError
3. Map hiển thị với markers
```

---

**Status:** ✅ Đã sync navbar.js sang Spring Boot static folder
