// Enhanced Navbar with User Dropdown
// Sử dụng window.API_BASE_URL để tránh conflict với các file khác
window.API_BASE_URL = window.API_BASE_URL || 'http://localhost:8080/api';


class Navbar {
    constructor() {
        this.user = this.getUserFromStorage();
        this.init();
    }

    getUserFromStorage() {
        return {
            token: localStorage.getItem('accessToken'),
            role: localStorage.getItem('userRole'),
            email: localStorage.getItem('userEmail'),
            name: localStorage.getItem('userName') || 'User',
            userId: localStorage.getItem('userId')
        };
    }

    init() {
        this.updateNavbar();
        this.attachEventListeners();
    }

    updateNavbar() {
        const navList = document.querySelector('.nav__list');
        if (!navList) return;

        if (this.user.token) {
            // User is logged in
            this.renderLoggedInNav(navList);
        } else {
            // User is not logged in
            this.renderLoggedOutNav(navList);
        }
    }

    renderLoggedInNav(navList) {
        navList.innerHTML = `
            <li class="nav__item">
                <a class="navbar__link" href="./index.html">
                    <i class="fas fa-map-marked-alt"></i>
                    <span>Bản đồ</span>
                </a>
            </li>
            <li class="nav__item">
                <a class="navbar__link" href="./qr-scanner.html">
                    <i class="fas fa-qrcode"></i>
                    <span>Quét QR</span>
                </a>
            </li>
            <li class="nav__item">
                <a class="navbar__link" href="./charging-status.html">
                    <i class="fas fa-charging-station"></i>
                    <span>Trạng thái</span>
                </a>
            </li>
            <li class="nav__item">
                <a class="navbar__link" href="./payment.html">
                    <i class="fas fa-wallet"></i>
                    <span>Thanh toán</span>
                </a>
            </li>
            <li class="nav__item nav__item--user">
                <button class="navbar__user-btn" id="userMenuBtn">
                    <div class="user-avatar">
                        <i class="fas fa-user"></i>
                    </div>
                    <span class="user-name">${this.escapeHtml(this.user.name)}</span>
                    <i class="fas fa-chevron-down"></i>
                </button>
                <div class="user-dropdown" id="userDropdown">
                    <div class="dropdown-header">
                        <div class="dropdown-avatar">
                            <i class="fas fa-user-circle"></i>
                        </div>
                        <div class="dropdown-info">
                            <div class="dropdown-name">${this.escapeHtml(this.user.name)}</div>
                            <div class="dropdown-email">${this.escapeHtml(this.user.email)}</div>
                        </div>
                    </div>
                    <div class="dropdown-divider"></div>
                    <a href="./profile.html" class="dropdown-item">
                        <i class="fas fa-user"></i>
                        <span>Hồ sơ</span>
                    </a>
                    <a href="./analytics.html" class="dropdown-item">
                        <i class="fas fa-history"></i>
                        <span>Lịch sử</span>
                    </a>
                    <a href="#" class="dropdown-item" id="settingsBtn">
                        <i class="fas fa-cog"></i>
                        <span>Cài đặt</span>
                    </a>
                    <div class="dropdown-divider"></div>
                    <button class="dropdown-item dropdown-item--danger" id="logoutBtn">
                        <i class="fas fa-sign-out-alt"></i>
                        <span>Đăng xuất</span>
                    </button>
                </div>
            </li>
        `;
    }

    renderLoggedOutNav(navList) {
        navList.innerHTML = `
            <li class="nav__item">
                <a class="navbar__link" href="./index.html">
                    <i class="fas fa-map-marked-alt"></i>
                    <span>Bản đồ</span>
                </a>
            </li>
            <li class="nav__item">
                <a class="navbar__link" href="./register.html">
                    <i class="fas fa-user-plus"></i>
                    <span>Đăng ký</span>
                </a>
            </li>
            <li class="nav__item">
                <a class="navbar__link navbar__link--primary" href="./login.html">
                    <i class="fas fa-sign-in-alt"></i>
                    <span>Đăng nhập</span>
                </a>
            </li>
        `;
    }

    attachEventListeners() {
        // User menu toggle
        document.addEventListener('click', (e) => {
            const userMenuBtn = document.getElementById('userMenuBtn');
            const userDropdown = document.getElementById('userDropdown');
            
            if (userMenuBtn && e.target.closest('#userMenuBtn')) {
                userDropdown.classList.toggle('show');
            } else if (userDropdown) {
                userDropdown.classList.remove('show');
            }
        });

        // Logout handler
        document.addEventListener('click', (e) => {
            if (e.target.closest('#logoutBtn')) {
                e.preventDefault();
                this.handleLogout();
            }
        });

        // Mobile menu toggle
        const menuIcon = document.getElementById('menu-icon');
        const headerNav = document.querySelector('.header__nav');
        
        if (menuIcon) {
            menuIcon.addEventListener('click', () => {
                headerNav.classList.toggle('active');
            });
        }
    }

    handleLogout() {
        if (confirm('Bạn có chắc muốn đăng xuất?')) {
            // Clear localStorage
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('userRole');
            localStorage.removeItem('userEmail');
            localStorage.removeItem('userName');
            localStorage.removeItem('userId');
            localStorage.removeItem('loginTime');
            
            // Redirect to login
            window.location.href = './login.html';
        }
    }

    escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, m => map[m]);
    }
}

// Initialize navbar when DOM is loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        new Navbar();
    });
} else {
    new Navbar();
}

// Add styles for the dropdown
const style = document.createElement('style');
style.textContent = `
    /* User Menu Styles */
    .nav__item--user {
        position: relative;
    }
    
    .navbar__user-btn {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        background: transparent;
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 8px;
        color: white;
        cursor: pointer;
        transition: all 0.3s;
        font-family: inherit;
    }
    
    .navbar__user-btn:hover {
        background: rgba(255, 255, 255, 0.1);
        border-color: rgba(255, 255, 255, 0.3);
    }
    
    .user-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 14px;
    }
    
    .user-name {
        font-weight: 600;
        font-size: 14px;
    }
    
    .navbar__user-btn i.fa-chevron-down {
        font-size: 12px;
        transition: transform 0.3s;
    }
    
    .navbar__user-btn:hover i.fa-chevron-down {
        transform: translateY(2px);
    }
    
    .user-dropdown {
        position: absolute;
        top: calc(100% + 8px);
        right: 0;
        min-width: 260px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
        opacity: 0;
        visibility: hidden;
        transform: translateY(-10px);
        transition: all 0.3s ease;
        z-index: 1000;
    }
    
    .user-dropdown.show {
        opacity: 1;
        visibility: visible;
        transform: translateY(0);
    }
    
    .dropdown-header {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12px 12px 0 0;
        color: white;
    }
    
    .dropdown-avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
    }
    
    .dropdown-info {
        flex: 1;
    }
    
    .dropdown-name {
        font-weight: 700;
        font-size: 16px;
        margin-bottom: 4px;
    }
    
    .dropdown-email {
        font-size: 13px;
        opacity: 0.9;
    }
    
    .dropdown-divider {
        height: 1px;
        background: #e5e7eb;
        margin: 8px 0;
    }
    
    .dropdown-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        color: #374151;
        text-decoration: none;
        transition: all 0.2s;
        cursor: pointer;
        border: none;
        background: transparent;
        width: 100%;
        text-align: left;
        font-family: inherit;
        font-size: 14px;
    }
    
    .dropdown-item:hover {
        background: #f3f4f6;
    }
    
    .dropdown-item i {
        width: 20px;
        text-align: center;
        font-size: 16px;
    }
    
    .dropdown-item--danger {
        color: #ef4444;
    }
    
    .dropdown-item--danger:hover {
        background: #fef2f2;
    }
    
    .navbar__link {
        display: flex;
        align-items: center;
        gap: 6px;
    }
    
    .navbar__link i {
        font-size: 16px;
    }
    
    .navbar__link--primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        padding: 8px 16px;
        border-radius: 8px;
        font-weight: 600;
    }
    
    @media (max-width: 768px) {
        .user-dropdown {
            right: -10px;
        }
    }
`;
document.head.appendChild(style);
