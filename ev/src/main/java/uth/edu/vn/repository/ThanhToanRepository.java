package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.ThanhToan;
import uth.edu.vn.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long> {
    
    // Note: ThanhToan entity doesn't have userId field, this method needs to be revised
    // List<ThanhToan> findByUserId(Long userId);
    
    List<ThanhToan> findByStatus(PaymentStatus status);
    
    @Query("SELECT t FROM ThanhToan t WHERE t.sessionId = :sessionId")
    List<ThanhToan> findBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT SUM(t.amount) FROM ThanhToan t WHERE t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Note: ThanhToan entity doesn't have user field, this query needs to be revised based on business logic
    // @Query("SELECT SUM(t.amount) FROM ThanhToan t WHERE t.user.id = :userId AND t.status = 'COMPLETED'")
    // Double getTotalSpentByUser(@Param("userId") Long userId);
}
