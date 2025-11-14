package uth.edu.vn.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Bad Request Exception
 * Throw khi request không hợp lệ
 * Trả về HTTP 400 Bad Request
 * 
 * Dùng cho:
 * - Email đã tồn tại khi đăng ký
 * - Input không hợp lệ (ngoài validation @Valid)
 * - Business logic error (VD: đặt chỗ mà trạm đã đầy)
 * - Trạng thái không hợp lệ (VD: hủy booking đã hoàn thành)
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
