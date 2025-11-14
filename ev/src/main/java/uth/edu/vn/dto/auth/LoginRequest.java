package uth.edu.vn.dto.auth;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Login Request DTO
 * Dùng để nhận thông tin đăng nhập từ client
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    
    // Optional: Remember me flag (để extend token expiration sau này)
    private boolean rememberMe = false;
}
