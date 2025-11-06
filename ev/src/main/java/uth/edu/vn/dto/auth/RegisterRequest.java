package uth.edu.vn.dto.auth;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Register Request DTO
 * Dùng để nhận dữ liệu đăng ký từ client
 */
@Data
public class RegisterRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Mật khẩu phải có chữ thường, chữ hoa và số"
    )
    private String password;
    
    @NotBlank(message = "Họ không được để trống")
    @Size(min = 1, max = 50, message = "Họ phải từ 1-50 ký tự")
    private String firstName;
    
    @NotBlank(message = "Tên không được để trống")
    @Size(min = 1, max = 50, message = "Tên phải từ 1-50 ký tự")
    private String lastName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
        regexp = "^(\\+84|0)[0-9]{9,10}$",
        message = "Số điện thoại không hợp lệ (VD: 0912345678 hoặc +84912345678)"
    )
    private String phoneNumber;
    
    // Role mặc định là EV_DRIVER khi đăng ký
    // Admin sẽ tạo CS_STAFF và ADMIN qua endpoint riêng
    private String role = "EV_DRIVER";
    
    // Thông tin xe (optional khi đăng ký, có thể cập nhật sau)
    private String vehicleModel;
    private String vehiclePlate;
    private String connectorType; // CCS, CHAdeMO, AC
}
