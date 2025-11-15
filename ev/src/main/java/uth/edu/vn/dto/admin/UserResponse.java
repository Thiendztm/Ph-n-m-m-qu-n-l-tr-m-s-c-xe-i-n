package uth.edu.vn.dto.admin;

import uth.edu.vn.entity.User;
import uth.edu.vn.enums.UserRole;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private Double walletBalance;
    private LocalDateTime createdAt;

    // ==================== UTILITY METHOD ====================
    /**
     * Chuyển đổi an toàn Object sang Double, xử lý null và BigDecimal.
     */
    private static Double safeConvertToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        // Xử lý BigDecimal (kiểu dữ liệu phổ biến của SQL DECIMAL/NUMERIC trong Java)
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        // Xử lý các kiểu số khác (Long, Integer, Float, Double)
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        // Trường hợp không phải số
        return 0.0;
    }
    // ========================================================

    // Constructors
    public UserResponse() {
    }

    /**
     * Constructor từ Entity.
     * 
     * @param user Entity User
     */
    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phone = user.getPhone();
        this.role = user.getRole();

        // SỬ DỤNG PHƯƠNG THỨC TRỢ GIÚP ĐỂ CHUYỂN ĐỔI AN TOÀN
        try {
            // Lấy Object từ Entity, sau đó chuyển đổi
            Object balanceObj = user.getWalletBalance();
            this.walletBalance = safeConvertToDouble(balanceObj);
        } catch (Exception e) {
            // Nếu không tìm thấy method getWalletBalance() hoặc lỗi khác
            this.walletBalance = 0.0;
        }

        try {
            this.createdAt = user.getCreatedAt();
        } catch (Exception e) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public UserResponse(Long id, String email, String firstName, String lastName, String phone, UserRole role,
            Double walletBalance, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
        this.walletBalance = walletBalance;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}