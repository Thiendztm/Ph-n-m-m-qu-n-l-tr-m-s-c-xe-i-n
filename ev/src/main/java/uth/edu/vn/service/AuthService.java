package uth.edu.vn.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uth.edu.vn.dto.auth.AuthResponse;
import uth.edu.vn.dto.auth.LoginRequest;
import uth.edu.vn.dto.auth.RegisterRequest;
import uth.edu.vn.entity.User;
import uth.edu.vn.enums.UserRole;
import uth.edu.vn.exception.BadRequestException;
import uth.edu.vn.exception.UnauthorizedException;
import uth.edu.vn.security.JwtTokenProvider;
import uth.edu.vn.util.HibernateUtil;

/**
 * Authentication Service
 * Xử lý logic đăng ký, đăng nhập, và tạo JWT tokens
 */
@Service
public class AuthService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * Đăng ký user mới (EV Driver)
     */
    public AuthResponse register(RegisterRequest request) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // 1. Check email đã tồn tại chưa
            Query<Long> checkQuery = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            checkQuery.setParameter("email", request.getEmail());
            Long count = checkQuery.uniqueResult();
            
            if (count > 0) {
                throw new BadRequestException("Email đã được sử dụng: " + request.getEmail());
            }
            
            // 2. Tạo User entity
            User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()), // Hash password với BCrypt
                request.getFirstName(),
                request.getLastName(),
                UserRole.EV_DRIVER // Mặc định là EV_DRIVER
            );
            
            // Set thông tin bổ sung
            user.setPhone(request.getPhoneNumber());
            
            // 3. Lưu vào database
            session.save(user);
            transaction.commit();
            
            // 4. Generate JWT tokens
            String accessToken = tokenProvider.generateTokenFromUsername(user.getEmail());
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
            
            // 5. Trả về AuthResponse
            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours in seconds
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .phoneNumber(user.getPhone())
                .build();
                
        } catch (BadRequestException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e; // Re-throw để GlobalExceptionHandler bắt
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Lỗi khi đăng ký: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }
    
    /**
     * Đăng nhập
     */
    public AuthResponse login(LoginRequest request) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            // 1. Authenticate với Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            
            // 2. Set authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 3. Load user từ database
            Query<User> query = session.createQuery(
                "FROM User WHERE email = :email", User.class);
            query.setParameter("email", request.getEmail());
            User user = query.uniqueResult();
            
            if (user == null) {
                throw new UnauthorizedException("User không tồn tại");
            }
            
            // 4. Generate JWT tokens
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
            
            // 5. Trả về AuthResponse
            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .phoneNumber(user.getPhone())
                .build();
                
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new UnauthorizedException("Email hoặc mật khẩu không đúng");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đăng nhập: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }
    
    /**
     * Refresh Access Token bằng Refresh Token
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // 1. Validate refresh token
            if (!tokenProvider.validateToken(refreshToken)) {
                throw new UnauthorizedException("Refresh token không hợp lệ hoặc đã hết hạn");
            }
            
            // 2. Lấy username từ token
            String email = tokenProvider.getUsernameFromToken(refreshToken);
            
            // 3. Generate access token mới
            String newAccessToken = tokenProvider.generateTokenFromUsername(email);
            
            // 4. Trả về token mới
            return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Giữ nguyên refresh token cũ
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();
                
        } catch (Exception e) {
            throw new UnauthorizedException("Không thể refresh token: " + e.getMessage());
        }
    }
}
