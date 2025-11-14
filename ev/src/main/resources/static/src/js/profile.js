// ========== CONFIG ==========
const API_BASE_URL = 'http://localhost:8080/api';

// ========== AUTHENTICATION CHECK ==========
function checkAuthentication() {
    const token = getAuthToken();
    if (!token) {
        // Redirect to login if not authenticated
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function logout() {
    // Clear auth tokens
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userInfo');
    
    // Show logout message
    showMessage('Đã đăng xuất thành công', true);
    
    // Redirect to login page
    setTimeout(() => {
        window.location.href = 'login.html';
    }, 1000);
}

// ========== DOM ELEMENTS ==========
const displayName = document.getElementById('displayName');
const displayEmail = document.getElementById('displayEmail');
const displayPhone = document.getElementById('displayPhone');
const displayVehicle = document.getElementById('displayVehicle');

const editBtn = document.getElementById('editBtn');
const cancelBtn = document.getElementById('cancelEdit');
const infoView = document.getElementById('infoView');
const infoEdit = document.getElementById('infoEdit');
const editForm = document.getElementById('editProfileForm');

const editName = document.getElementById('editName');
const editEmail = document.getElementById('editEmail');
const editPhone = document.getElementById('editPhone');
const editVehicle = document.getElementById('editVehicle');

// ========== UTILITY FUNCTIONS ==========
function getAuthToken() {
    const token = localStorage.getItem('accessToken');
    console.log('Getting auth token:', token ? 'Token exists' : 'No token found');
    console.log('Full token:', token);
    return token;
}

function debugTokens() {
    console.log('=== TOKEN DEBUG ===');
    console.log('Access Token:', localStorage.getItem('accessToken'));
    console.log('Refresh Token:', localStorage.getItem('refreshToken'));
    console.log('User Info:', localStorage.getItem('userInfo'));
    console.log('All localStorage keys:', Object.keys(localStorage));
}

function showMessage(message, isSuccess = true) {
    // Tạo toast notification
    const toast = document.createElement('div');
    toast.className = `toast ${isSuccess ? 'success' : 'error'}`;
    toast.textContent = message;
    
    // Thêm CSS cho toast
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        color: white;
        font-weight: 500;
        z-index: 9999;
        animation: slideIn 0.3s ease;
        background: ${isSuccess ? '#4CAF50' : '#f44336'};
    `;
    
    document.body.appendChild(toast);
    
    // Tự động xóa sau 3 giây
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { 
        style: 'currency', 
        currency: 'VND' 
    }).format(amount);
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit', 
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ========== API FUNCTIONS ==========
async function loadProfile() {
    try {
        const token = getAuthToken();
        if (!token) {
            window.location.href = './login.html';
            return;
        }

        const response = await fetch(`${API_BASE_URL}/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const profile = await response.json();
            displayProfile(profile);
        } else if (response.status === 401) {
            // Token expired
            localStorage.clear();
            window.location.href = './login.html';
        } else {
            throw new Error('Không thể tải thông tin hồ sơ');
        }
    } catch (error) {
        console.error('Error loading profile:', error);
        showMessage('Lỗi khi tải thông tin hồ sơ: ' + error.message, false);
    }
}

async function updateProfile(profileData) {
    try {
        const token = getAuthToken();
        if (!token) {
            window.location.href = './login.html';
            return;
        }

        const response = await fetch(`${API_BASE_URL}/profile`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(profileData)
        });

        const result = await response.json();

        if (response.ok) {
            showMessage('Cập nhật thông tin thành công!');
            loadProfile(); // Reload profile
            toggleEditMode(false);
        } else {
            throw new Error(result.error || 'Không thể cập nhật thông tin');
        }
    } catch (error) {
        console.error('Error updating profile:', error);
        showMessage('Lỗi khi cập nhật: ' + error.message, false);
    }
}

async function loadChargingHistory() {
    try {
        const token = getAuthToken();
        if (!token) return;

        const response = await fetch(`${API_BASE_URL}/history/charging`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            displayChargingHistory(data.sessions);
        }
    } catch (error) {
        console.error('Error loading charging history:', error);
    }
}

// ========== UI FUNCTIONS ==========
function displayProfile(data) {
    console.log('Displaying profile data:', data);
    
    const user = data.user;
    const vehicle = data.vehicle;
    
    // Hiển thị thông tin cơ bản từ database
    const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
    displayName.textContent = fullName || 'Chưa có tên';
    displayEmail.textContent = user.email || 'Chưa có email';
    displayPhone.textContent = user.phone || 'Chưa có số điện thoại';
    
    // Hiển thị biển số xe từ database
    if (vehicle && vehicle.plateNumber) {
        displayVehicle.textContent = vehicle.plateNumber;
    } else {
        displayVehicle.textContent = 'Chưa có thông tin xe';
    }

    // Cập nhật form edit với dữ liệu thực
    editName.value = fullName;
    editEmail.value = user.email || '';
    editPhone.value = user.phone || '';
    if (vehicle && vehicle.plateNumber) {
        const vehicleEditInfo = [];
        if (vehicle.make) vehicleEditInfo.push(vehicle.make);
        if (vehicle.model) vehicleEditInfo.push(vehicle.model);
        if (vehicle.plateNumber) vehicleEditInfo.push(`- ${vehicle.plateNumber}`);
        editVehicle.value = vehicleEditInfo.join(' ');
    } else {
        editVehicle.value = '';
    }
}

function displayChargingHistory(sessions) {
    const listContent = document.getElementById('listContent');
    const historyCount = document.getElementById('historyCount');
    
    if (!sessions || sessions.length === 0) {
        listContent.innerHTML = '<p class="no-data">Chưa có lịch sử sạc xe</p>';
        historyCount.textContent = '0 giao dịch';
        return;
    }

    historyCount.textContent = `${sessions.length} giao dịch`;

    const historyHTML = sessions.map(session => {
        const statusClass = session.status === 'COMPLETED' ? 'available' : 
                          session.status === 'CANCELLED' ? 'busy' : 'occupied';
        const statusText = session.status === 'COMPLETED' ? 'Hoàn thành' :
                          session.status === 'CANCELLED' ? 'Đã hủy' : 'Đang sạc';
        
        const stationName = session.station?.stationName || 'Trạm sạc';
        const energyText = session.energyConsumed ? 
            `${session.energyConsumed} kWh (${session.station?.chargerType || 'Unknown'})` :
            'Không có dữ liệu';
        const costText = session.totalCost ? 
            formatCurrency(session.totalCost) : '0đ';

        return `
            <div class="station-card">
                <div class="station-header">
                    <h4>${stationName}</h4>
                    <span class="status ${statusClass}">${statusText}</span>
                </div>
                <p class="address">
                    <i class="fa-solid fa-calendar-days"></i> 
                    ${formatDateTime(session.startTime)}
                </p>
                <p class="details">
                    <i class="fa-solid fa-bolt"></i> 
                    ${energyText}
                </p>
                <p class="distance charge-cost">${costText}</p>
            </div>
        `;
    }).join('');

    listContent.innerHTML = historyHTML;
}

function toggleEditMode(isEdit) {
    if (isEdit) {
        infoView.style.display = 'none';
        infoEdit.style.display = 'block';
    } else {
        infoView.style.display = 'block';
        infoEdit.style.display = 'none';
    }
}

// ========== EVENT LISTENERS ==========
document.addEventListener('DOMContentLoaded', function() {
    // Debug tokens first
    debugTokens();
    
    // Kiểm tra authentication
    const token = getAuthToken();
    if (!token) {
        console.error('No token found, redirecting to login');
        window.location.href = './login.html';
        return;
    }

    // Setup auth link (logout)
    const authLink = document.getElementById('authLink');
    if (authLink) {
        authLink.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    }

    // Load dữ liệu
    loadProfile();
    loadChargingHistory();

    // Edit mode toggle
    editBtn.addEventListener('click', () => toggleEditMode(true));
    cancelBtn.addEventListener('click', () => toggleEditMode(false));

    // Form submission
    editForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const fullName = editName.value.trim();
        const phone = editPhone.value.trim();
        const vehicleInfo = editVehicle.value.trim();

        if (!fullName || !phone) {
            showMessage('Vui lòng điền đầy đủ thông tin bắt buộc', false);
            return;
        }

        // Prepare profile data
        const nameParts = fullName.split(' ');
        const firstName = nameParts[0];
        const lastName = nameParts.slice(1).join(' ');

        const profileData = {
            firstName: firstName,
            lastName: lastName,
            phone: phone
        };

        await updateProfile(profileData);

        // Update vehicle info if provided
        if (vehicleInfo) {
            // Parse vehicle info (expecting format: "Make Model - PlateNumber")
            const vehicleParts = vehicleInfo.split(' - ');
            if (vehicleParts.length === 2) {
                const vehicleData = {
                    model: vehicleParts[0],
                    plateNumber: vehicleParts[1]
                };

                try {
                    const token = getAuthToken();
                    const response = await fetch(`${API_BASE_URL}/profile/vehicle`, {
                        method: 'PUT',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(vehicleData)
                    });

                    if (!response.ok) {
                        const error = await response.json();
                        console.warn('Vehicle update failed:', error);
                    }
                } catch (error) {
                    console.warn('Vehicle update error:', error);
                }
            }
        }
    });
});

// Add CSS for toast animation
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    .no-data {
        text-align: center;
        color: #666;
        font-style: italic;
        padding: 20px;
    }
`;
document.head.appendChild(style);