// ========== AUTH CHECK & NAVBAR UPDATE ==========
// File này sẽ được include trong index.html và các trang khác để check login status

function checkAuthStatus() {
  const accessToken = localStorage.getItem('accessToken');
  const userName = localStorage.getItem('userName');
  const userRole = localStorage.getItem('userRole');

  return {
    isLoggedIn: accessToken !== null,
    userName: userName,
    userRole: userRole,
    accessToken: accessToken
  };
}

function updateNavbar() {
  const auth = checkAuthStatus();
  const navbarContainer = document.querySelector('.navbar');

  if (!navbarContainer) return;

  // Find login/register buttons
  const loginBtn = document.querySelector('a[href="login.html"]');
  const registerBtn = document.querySelector('a[href="register.html"]');

  if (auth.isLoggedIn) {
    // User is logged in - show user menu
    if (loginBtn) {
      loginBtn.style.display = 'none';
    }
    if (registerBtn) {
      registerBtn.style.display = 'none';
    }

    // Add user dropdown (if not exists)
    let userMenu = document.querySelector('.user-menu');
    if (!userMenu) {
      userMenu = document.createElement('div');
      userMenu.className = 'user-menu';
      userMenu.innerHTML = `
        <div class="dropdown">
          <button class="btn btn-outline-primary dropdown-toggle" type="button" id="userDropdown" 
                  data-bs-toggle="dropdown" aria-expanded="false">
            <i class="fa-solid fa-user"></i> ${auth.userName || 'User'}
          </button>
          <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown">
            <li><a class="dropdown-item" href="profile.html">
              <i class="fa-solid fa-user-circle"></i> Tài khoản
            </a></li>
            <li><a class="dropdown-item" href="index.html#history">
              <i class="fa-solid fa-history"></i> Lịch sử sạc
            </a></li>
            ${auth.userRole === 'ADMIN' ? '<li><hr class="dropdown-divider"></li><li><a class="dropdown-item" href="analytics.html"><i class="fa-solid fa-chart-line"></i> Dashboard</a></li>' : ''}
            <li><hr class="dropdown-divider"></li>
            <li><a class="dropdown-item text-danger" href="#" id="logoutBtn">
              <i class="fa-solid fa-sign-out-alt"></i> Đăng xuất
            </a></li>
          </ul>
        </div>
      `;

      // Insert user menu after register button or at end of navbar
      if (registerBtn && registerBtn.parentElement) {
        registerBtn.parentElement.insertAdjacentElement('afterend', userMenu);
      } else if (navbarContainer.querySelector('.navbar-nav')) {
        navbarContainer.querySelector('.navbar-nav').appendChild(userMenu);
      }

      // Add logout handler
      document.getElementById('logoutBtn')?.addEventListener('click', handleLogout);
    }
  } else {
    // User is NOT logged in - show login/register buttons
    if (loginBtn) loginBtn.style.display = '';
    if (registerBtn) registerBtn.style.display = '';

    // Remove user menu if exists
    const userMenu = document.querySelector('.user-menu');
    if (userMenu) {
      userMenu.remove();
    }
  }
}

function handleLogout(e) {
  if (e) e.preventDefault();

  // Clear all auth data
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('userId');
  localStorage.removeItem('userEmail');
  localStorage.removeItem('userRole');
  localStorage.removeItem('userName');

  // Redirect to home
  window.location.href = 'index.html';
}

// Protect page (redirect to login if not authenticated)
function requireAuth() {
  const auth = checkAuthStatus();
  if (!auth.isLoggedIn) {
    window.location.href = 'login.html';
  }
}

// API call helper with automatic token injection
async function apiCall(endpoint, options = {}) {
  const auth = checkAuthStatus();
  
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
      ...(auth.isLoggedIn ? { 'Authorization': `Bearer ${auth.accessToken}` } : {})
    }
  };

  const mergedOptions = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultOptions.headers,
      ...options.headers
    }
  };

  try {
    const response = await fetch(`http://localhost:8080${endpoint}`, mergedOptions);
    
    // Handle 401 Unauthorized (token expired)
    if (response.status === 401) {
      // Try to refresh token
      const refreshed = await refreshAccessToken();
      if (refreshed) {
        // Retry the original request with new token
        mergedOptions.headers['Authorization'] = `Bearer ${localStorage.getItem('accessToken')}`;
        return await fetch(`http://localhost:8080${endpoint}`, mergedOptions);
      } else {
        // Refresh failed - logout
        handleLogout();
        return response;
      }
    }

    return response;
  } catch (error) {
    console.error('API call error:', error);
    throw error;
  }
}

// Refresh access token using refresh token
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) return false;

  try {
    const response = await fetch('http://localhost:8080/api/auth/refresh', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ refreshToken })
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('accessToken', data.accessToken);
      // Update refresh token if backend sends a new one
      if (data.refreshToken) {
        localStorage.setItem('refreshToken', data.refreshToken);
      }
      return true;
    }
    return false;
  } catch (error) {
    console.error('Token refresh error:', error);
    return false;
  }
}

// Initialize on page load
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', updateNavbar);
} else {
  updateNavbar();
}
