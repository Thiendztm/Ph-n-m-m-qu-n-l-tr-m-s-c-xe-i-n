package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.PhienSac;
import uth.edu.vn.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhienSacRepository extends JpaRepository<PhienSac, Long> {
    
    List<PhienSac> findByUserId(Long userId);
    
    List<PhienSac> findByStatus(SessionStatus status);
    
    Optional<PhienSac> findByQrCode(String qrCode);
    
    @Query("SELECT ps FROM PhienSac ps WHERE ps.user.id = :userId AND ps.status = :status")
    List<PhienSac> findByUserAndStatus(@Param("userId") Long userId, @Param("status") SessionStatus status);
    
    @Query("SELECT ps FROM PhienSac ps WHERE ps.user.id = :userId ORDER BY ps.startTime DESC")
    List<PhienSac> findByUserOrderByStartTimeDesc(@Param("userId") Long userId);
    
    @Query("SELECT ps FROM PhienSac ps WHERE ps.startTime BETWEEN :startDate AND :endDate")
    List<PhienSac> findSessionsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(ps.energyConsumed) FROM PhienSac ps WHERE ps.user.id = :userId AND ps.status = 'COMPLETED'")
    Double getTotalEnergyConsumedByUser(@Param("userId") Long userId);
    
    @Query("SELECT SUM(ps.totalCost) FROM PhienSac ps WHERE ps.user.id = :userId AND MONTH(ps.startTime) = :month AND YEAR(ps.startTime) = :year")
    Double getMonthlyChargingCost(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
    
    // ==================== Methods for AdminService ====================
    
    /**
     * Count sessions by status (for usage statistics)
     */
    Long countByStatus(SessionStatus status);
    
    /**
     * Get total revenue by charging station
     */
    @Query("SELECT SUM(ps.totalCost) FROM PhienSac ps WHERE ps.chargingPoint.chargingStation.id = :stationId AND ps.totalCost IS NOT NULL")
    Double getTotalRevenueByStation(@Param("stationId") Long stationId);
    
    /**
     * Get monthly session count
     */
    @Query("SELECT COUNT(ps) FROM PhienSac ps WHERE YEAR(ps.startTime) = :year AND MONTH(ps.startTime) = :month")
    Long getMonthlySessionCount(@Param("year") int year, @Param("month") int month);
    
    /**
     * Get monthly energy consumed
     */
    @Query("SELECT SUM(ps.energyConsumed) FROM PhienSac ps WHERE YEAR(ps.startTime) = :year AND MONTH(ps.startTime) = :month")
    Double getMonthlyEnergyConsumed(@Param("year") int year, @Param("month") int month);
    
    /**
     * Get monthly revenue
     */
    @Query("SELECT SUM(ps.totalCost) FROM PhienSac ps WHERE YEAR(ps.startTime) = :year AND MONTH(ps.startTime) = :month")
    Double getMonthlyRevenue(@Param("year") int year, @Param("month") int month);
}
