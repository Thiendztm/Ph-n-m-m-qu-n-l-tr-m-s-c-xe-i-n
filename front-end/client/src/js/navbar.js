// ========== NAVBAR MANAGEMENT ==========

// Check authentication status and update navbar
function updateNavbar() {
    const accessToken = localStorage.getItem('accessToken');
    const userName = localStorage.getItem('userName');
    const userRole = localStorage.getItem('userRole');
    
    console.log('updateNavbar called:', { accessToken: !!accessToken, userName, userRole });
    
    // Find the login/logout nav item
    const navList = document.querySelector('.nav__list');
    if (!navList) {
        console.log('navList not found');
        return;
    }
    
    const loginNavItem = navList.querySelector('li:last-child'); // Last item should be login
    if (!loginNavItem) {
        console.log('loginNavItem not found');
        return;
    }
    
    if (accessToken && userName) {
        // User is logged in - show user info and logout
        loginNavItem.innerHTML = `
            <div class="user-menu">
                <span class="user-name">
                    <i class="fa-solid fa-user"></i> ${userName}
                </span>
                <div class="user-dropdown">
                    <a class="navbar__link" href="./profile.html">
                        <i class="fa-solid fa-user-circle"></i> Hồ sơ
                    </a>
                    ${userRole === 'ADMIN' || userRole === 'CS_STAFF' ? 
                        '<a class="navbar__link" href="./admin/index.html"><i class="fa-solid fa-shield-halved"></i> Admin Panel</a>' : 
                        ''
                    }
                    <a class="navbar__link logout-btn" href="#" onclick="logout()">
                        <i class="fa-solid fa-sign-out-alt"></i> Đăng Xuất
                    </a>
                </div>
            </div>
        `;
        
        // Add CSS for user menu
        if (!document.getElementById('user-menu-styles')) {
            const style = document.createElement('style');
            style.id = 'user-menu-styles';
            style.textContent = `
                .user-menu {
                    position: relative;
                    cursor: pointer;
                }
                
                .user-name {
                    color: #fff;
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    padding: 8px 12px;
                    border-radius: 6px;
                    transition: background-color 0.3s;
                }
                
                .user-name:hover {
                    background-color: rgba(255, 255, 255, 0.1);
                }
                
                .user-dropdown {
                    position: absolute;
                    top: 100%;
                    right: 0;
                    background: #fff;
                    border-radius: 8px;
                    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
                    min-width: 200px;
                    opacity: 0;
                    visibility: hidden;
                    transform: translateY(-10px);
                    transition: all 0.3s ease;
                    z-index: 1000;
                }
                
                .user-menu:hover .user-dropdown {
                    opacity: 1;
                    visibility: visible;
                    transform: translateY(0);
                }
                
                .user-dropdown .navbar__link {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    padding: 12px 16px;
                    color: #333;
                    text-decoration: none;
                    border-bottom: 1px solid #eee;
                    transition: background-color 0.2s;
                }
                
                .user-dropdown .navbar__link:last-child {
                    border-bottom: none;
                }
                
                .user-dropdown .navbar__link:hover {
                    background-color: #f8f9fa;
                }
                
                .user-dropdown .logout-btn:hover {
                    background-color: #fee;
                    color: #dc3545;
                }
                
                @media (max-width: 768px) {
                    .user-dropdown {
                        position: static;
                        opacity: 1;
                        visibility: visible;
                        transform: none;
                        box-shadow: none;
                        background: transparent;
                    }
                    
                    .user-dropdown .navbar__link {
                        color: #fff;
                        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
                    }
                    
                    .user-dropdown .navbar__link:hover {
                        background-color: rgba(255, 255, 255, 0.1);
                    }
                }
            `;
            document.head.appendChild(style);
        }
        
    } else {
        // User is not logged in - show login link
        loginNavItem.innerHTML = `
            <a class="navbar__link" href="./login.html">
                <i class="fa-solid fa-sign-in-alt"></i> Đăng Nhập
            </a>
        `;
    }
}

// Logout function
function logout() {
    // Clear auth data
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userName');
    
    // Show logout message
    alert('Đăng xuất thành công!');
    
    // Redirect to home page
    window.location.href = 'index.html';
}

// Initialize navbar when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, calling updateNavbar');
    updateNavbar();
});

// Also call immediately if DOM already loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', updateNavbar);
} else {
    console.log('DOM already loaded, calling updateNavbar immediately');
    updateNavbar();
}

// Update navbar when storage changes (e.g., login from another tab)
window.addEventListener('storage', function(e) {
    if (e.key === 'accessToken' || e.key === 'userName') {
        updateNavbar();
    }
});