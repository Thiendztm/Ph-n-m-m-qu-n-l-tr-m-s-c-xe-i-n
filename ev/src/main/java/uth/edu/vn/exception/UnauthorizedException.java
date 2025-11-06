package uth.edu.vn.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Unauthorized Exception
 * Throw khi user không có quyền truy cập
 * Trả về HTTP 401 Unauthorized
 * 
 * Dùng cho:
 * - Sai password khi login
 * - JWT token invalid hoặc expired
 * - User không có quyền thực hiện action
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
