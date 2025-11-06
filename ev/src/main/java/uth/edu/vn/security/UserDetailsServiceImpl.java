package uth.edu.vn.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.entity.User;
import uth.edu.vn.util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDetailsService Implementation
 * Spring Security dùng class này để load user từ database
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Load user từ database qua email
        User user = findUserByEmail(email);
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
        // Convert User entity sang UserDetails (Spring Security format)
        return buildUserDetails(user);
    }
    
    /**
     * Tìm user trong database bằng email
     */
    private User findUserByEmail(String email) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("Error finding user by email: " + email, e);
            return null;
        }
    }
    
    /**
     * Convert User entity sang Spring Security UserDetails
     */
    private UserDetails buildUserDetails(User user) {
        // Tạo authorities (roles) cho user
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role của user (EV_DRIVER, CS_STAFF, ADMIN)
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }
        
        // Return UserDetails object
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // Dùng email làm username
                .password(user.getPassword()) // Password đã được hash
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
