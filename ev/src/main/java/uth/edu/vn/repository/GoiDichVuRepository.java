package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.GoiDichVu;
import uth.edu.vn.entity.User;
import uth.edu.vn.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoiDichVuRepository extends JpaRepository<GoiDichVu, Long> {
    
    /**
     * Find all subscriptions by user
     */
    List<GoiDichVu> findByUser(User user);
    
    /**
     * Find subscriptions by user ID
     */
    List<GoiDichVu> findByUserId(Long userId);
    
    /**
     * Find subscriptions by status
     */
    List<GoiDichVu> findByStatus(SubscriptionStatus status);
    
    /**
     * Find active subscription for user
     */
    @Query("SELECT g FROM GoiDichVu g " +
           "WHERE g.user.id = :userId " +
           "AND g.status = 'ACTIVE' " +
           "AND (g.endDate IS NULL OR g.endDate > :currentDate)")
    Optional<GoiDichVu> findActiveSubscriptionByUser(@Param("userId") Long userId, 
                                                      @Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find all active subscriptions
     */
    @Query("SELECT g FROM GoiDichVu g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND (g.endDate IS NULL OR g.endDate > :currentDate)")
    List<GoiDichVu> findAllActiveSubscriptions(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find subscriptions expiring soon (within next 7 days)
     */
    @Query("SELECT g FROM GoiDichVu g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND g.endDate IS NOT NULL " +
           "AND g.endDate BETWEEN :now AND :weekLater")
    List<GoiDichVu> findExpiringSoon(@Param("now") LocalDateTime now, 
                                     @Param("weekLater") LocalDateTime weekLater);
    
    /**
     * Count active subscriptions
     */
    @Query("SELECT COUNT(g) FROM GoiDichVu g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND (g.endDate IS NULL OR g.endDate > :currentDate)")
    Long countActiveSubscriptions(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Get total monthly revenue from subscriptions
     */
    @Query("SELECT COALESCE(SUM(g.monthlyFee), 0) FROM GoiDichVu g " +
           "WHERE g.status = 'ACTIVE' " +
           "AND (g.endDate IS NULL OR g.endDate > :currentDate)")
    BigDecimal getTotalMonthlyRevenue(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find subscriptions by plan name
     */
    List<GoiDichVu> findByPlanName(String planName);
    
    /**
     * Find subscriptions created in date range
     */
    @Query("SELECT g FROM GoiDichVu g " +
           "WHERE g.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY g.createdAt DESC")
    List<GoiDichVu> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Check if user has active subscription
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
           "FROM GoiDichVu g " +
           "WHERE g.user.id = :userId " +
           "AND g.status = 'ACTIVE' " +
           "AND (g.endDate IS NULL OR g.endDate > :currentDate)")
    boolean hasActiveSubscription(@Param("userId") Long userId, 
                                  @Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find subscriptions by user and status
     */
    List<GoiDichVu> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    /**
     * Get subscription statistics by plan
     */
    @Query("SELECT g.planName, COUNT(g), SUM(g.monthlyFee) " +
           "FROM GoiDichVu g " +
           "WHERE g.status = 'ACTIVE' " +
           "GROUP BY g.planName " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getSubscriptionStatistics();
}
