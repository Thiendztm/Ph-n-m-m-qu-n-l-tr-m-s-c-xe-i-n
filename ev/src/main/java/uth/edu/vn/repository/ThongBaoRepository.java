package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.entity.ThongBao;
import uth.edu.vn.entity.User;
import uth.edu.vn.enums.NotificationType;
import uth.edu.vn.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {
    
    /**
     * Find all notifications by user
     */
    List<ThongBao> findByUser(User user);
    
    /**
     * Find notifications by user ID ordered by sent date descending
     */
    List<ThongBao> findByUserIdOrderBySentAtDesc(Long userId);
    
    /**
     * Find notifications by status
     */
    List<ThongBao> findByStatus(NotificationStatus status);
    
    /**
     * Find unread notifications by user
     */
    @Query("SELECT n FROM ThongBao n " +
           "WHERE n.user.id = :userId " +
           "AND n.status = 'UNREAD' " +
           "ORDER BY n.sentAt DESC")
    List<ThongBao> findUnreadByUser(@Param("userId") Long userId);
    
    /**
     * Find notifications by user and status
     */
    List<ThongBao> findByUserIdAndStatus(Long userId, NotificationStatus status);
    
    /**
     * Find notifications by user and type
     */
    List<ThongBao> findByUserIdAndType(Long userId, NotificationType type);
    
    /**
     * Count unread notifications for user
     */
    @Query("SELECT COUNT(n) FROM ThongBao n " +
           "WHERE n.user.id = :userId " +
           "AND n.status = 'UNREAD'")
    Long countUnreadByUser(@Param("userId") Long userId);
    
    /**
     * Mark notification as read
     */
    @Modifying
    @Transactional
    @Query("UPDATE ThongBao n " +
           "SET n.status = 'READ', n.readAt = :readAt " +
           "WHERE n.notificationId = :notificationId")
    int markAsRead(@Param("notificationId") Long notificationId, 
                   @Param("readAt") LocalDateTime readAt);
    
    /**
     * Mark all user notifications as read
     */
    @Modifying
    @Transactional
    @Query("UPDATE ThongBao n " +
           "SET n.status = 'READ', n.readAt = :readAt " +
           "WHERE n.user.id = :userId " +
           "AND n.status = 'UNREAD'")
    int markAllAsReadByUser(@Param("userId") Long userId, 
                           @Param("readAt") LocalDateTime readAt);
    
    /**
     * Delete old notifications (older than specified date)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ThongBao n " +
           "WHERE n.sentAt < :cutoffDate " +
           "AND n.status != 'UNREAD'")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find recent notifications with limit
     */
    @Query("SELECT n FROM ThongBao n " +
           "WHERE n.user.id = :userId " +
           "ORDER BY n.sentAt DESC")
    List<ThongBao> findRecentByUser(@Param("userId") Long userId, 
                                    org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find notifications by date range
     */
    @Query("SELECT n FROM ThongBao n " +
           "WHERE n.user.id = :userId " +
           "AND n.sentAt BETWEEN :startDate AND :endDate " +
           "ORDER BY n.sentAt DESC")
    List<ThongBao> findByUserAndDateRange(@Param("userId") Long userId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find notifications by type and status
     */
    List<ThongBao> findByTypeAndStatus(NotificationType type, NotificationStatus status);
    
    /**
     * Count notifications by user and type
     */
    @Query("SELECT COUNT(n) FROM ThongBao n " +
           "WHERE n.user.id = :userId " +
           "AND n.type = :type")
    Long countByUserAndType(@Param("userId") Long userId, 
                           @Param("type") NotificationType type);
    
    /**
     * Find urgent unread notifications (alert type)
     */
    @Query("SELECT n FROM ThongBao n " +
           "WHERE n.user.id = :userId " +
           "AND n.status = 'UNREAD' " +
           "AND n.type IN ('ALERT', 'EMERGENCY') " +
           "ORDER BY n.sentAt DESC")
    List<ThongBao> findUrgentUnreadByUser(@Param("userId") Long userId);
}
