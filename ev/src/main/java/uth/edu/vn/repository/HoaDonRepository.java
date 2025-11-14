package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.HoaDon;
import uth.edu.vn.entity.ThanhToan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {
    
    /**
     * Find invoice by payment
     */
    Optional<HoaDon> findByPayment(ThanhToan payment);
    
    /**
     * Find invoice by invoice number
     */
    Optional<HoaDon> findByInvoiceNumber(String invoiceNumber);
    
    /**
     * Find all invoices by user through payment relationship
     */
    @Query("SELECT h FROM HoaDon h WHERE h.payment.sessionId IN " +
           "(SELECT ps.sessionId FROM PhienSac ps WHERE ps.user.id = :userId)")
    List<HoaDon> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find invoices by date range
     */
    @Query("SELECT h FROM HoaDon h WHERE h.issueDate BETWEEN :startDate AND :endDate " +
           "ORDER BY h.issueDate DESC")
    List<HoaDon> findByIssueDateBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total amount by user
     */
    @Query("SELECT COALESCE(SUM(h.totalAmount), 0) FROM HoaDon h " +
           "WHERE h.payment.sessionId IN " +
           "(SELECT ps.sessionId FROM PhienSac ps WHERE ps.user.id = :userId)")
    BigDecimal getTotalAmountByUser(@Param("userId") Long userId);
    
    /**
     * Get monthly invoices for user
     */
    @Query("SELECT h FROM HoaDon h " +
           "WHERE h.payment.sessionId IN " +
           "(SELECT ps.sessionId FROM PhienSac ps WHERE ps.user.id = :userId) " +
           "AND YEAR(h.issueDate) = :year AND MONTH(h.issueDate) = :month " +
           "ORDER BY h.issueDate DESC")
    List<HoaDon> findMonthlyInvoicesByUser(@Param("userId") Long userId, 
                                          @Param("year") int year, 
                                          @Param("month") int month);
    
    /**
     * Find overdue invoices (due date passed and not paid)
     */
    @Query("SELECT h FROM HoaDon h " +
           "WHERE h.dueDate < :currentDate " +
           "AND h.payment.status = 'PENDING' " +
           "ORDER BY h.dueDate ASC")
    List<HoaDon> findOverdueInvoices(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Get total revenue in date range
     */
    @Query("SELECT COALESCE(SUM(h.totalAmount), 0) FROM HoaDon h " +
           "WHERE h.issueDate BETWEEN :startDate AND :endDate " +
           "AND h.payment.status = 'COMPLETED'")
    BigDecimal getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count invoices by date range
     */
    @Query("SELECT COUNT(h) FROM HoaDon h " +
           "WHERE h.issueDate BETWEEN :startDate AND :endDate")
    Long countByIssueDateBetween(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find recent invoices with limit
     */
    @Query("SELECT h FROM HoaDon h ORDER BY h.issueDate DESC")
    List<HoaDon> findRecentInvoices(org.springframework.data.domain.Pageable pageable);
}
