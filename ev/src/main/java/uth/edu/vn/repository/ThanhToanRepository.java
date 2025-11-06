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
    
    List<ThanhToan> findByUserId(Long userId);
    
    List<ThanhToan> findByStatus(PaymentStatus status);
    
    @Query("SELECT t FROM ThanhToan t WHERE t.session.sessionId = :sessionId")
    List<ThanhToan> findBySessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT SUM(t.amount) FROM ThanhToan t WHERE t.status = 'SUCCESS' AND t.paymentDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM ThanhToan t WHERE t.user.id = :userId AND t.status = 'SUCCESS'")
    Double getTotalSpentByUser(@Param("userId") Long userId);
}
