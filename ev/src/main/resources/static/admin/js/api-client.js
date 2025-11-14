// ========== API CLIENT UTILITY ==========
// Centralized API calling with JWT authentication

const API_BASE_URL = 'http://localhost:8080/api';

// Get tokens from localStorage
function getAccessToken() {
  return localStorage.getItem('accessToken');
}

function getRefreshToken() {
  return localStorage.getItem('refreshToken');
}

function setTokens(accessToken, refreshToken) {
  localStorage.setItem('accessToken', accessToken);
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken);
  }
}

function clearTokens() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('userName');
  localStorage.removeItem('userRole');
  localStorage.removeItem('userId');
}

// Refresh access token
async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken })
    });

    if (!response.ok) {
      throw new Error('Token refresh failed');
    }

    const data = await response.json();
    setTokens(data.accessToken, data.refreshToken);
    return data.accessToken;
  } catch (error) {
    clearTokens();
    window.location.href = './login.html';
    throw error;
  }
}

// Main API call function with automatic token refresh
async function apiCall(endpoint, options = {}) {
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;
  
  // Add default headers
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };

  // Add authorization header if token exists
  const accessToken = getAccessToken();
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }

  const requestOptions = {
    ...options,
    headers
  };

  try {
    let response = await fetch(url, requestOptions);

    // If unauthorized, try to refresh token
    if (response.status === 401) {
      const newToken = await refreshAccessToken();
      headers['Authorization'] = `Bearer ${newToken}`;
      requestOptions.headers = headers;
      response = await fetch(url, requestOptions);
    }

    // Handle non-OK responses
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP Error: ${response.status}`);
    }

    // Return parsed JSON or null for 204 No Content
    if (response.status === 204) {
      return null;
    }

    return await response.json();
  } catch (error) {
    console.error('API call failed:', error);
    throw error;
  }
}

// Convenience methods
const api = {
  // GET request
  get: (endpoint, options = {}) => {
    return apiCall(endpoint, { ...options, method: 'GET' });
  },

  // POST request
  post: (endpoint, data, options = {}) => {
    return apiCall(endpoint, {
      ...options,
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // PUT request
  put: (endpoint, data, options = {}) => {
    return apiCall(endpoint, {
      ...options,
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  // DELETE request
  delete: (endpoint, options = {}) => {
    return apiCall(endpoint, { ...options, method: 'DELETE' });
  },

  // PATCH request
  patch: (endpoint, data, options = {}) => {
    return apiCall(endpoint, {
      ...options,
      method: 'PATCH',
      body: JSON.stringify(data)
    });
  }
};

// Export for ES6 modules
export { api, setTokens, clearTokens, getAccessToken, API_BASE_URL };
