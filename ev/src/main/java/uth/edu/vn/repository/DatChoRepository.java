package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.DatCho;
import uth.edu.vn.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DatChoRepository extends JpaRepository<DatCho, Long> {
    
    List<DatCho> findByUserId(Long userId);
    
    List<DatCho> findByStatus(BookingStatus status);
    
    @Query("SELECT dc FROM DatCho dc WHERE dc.user.id = :userId AND dc.status = :status")
    List<DatCho> findByUserAndStatus(@Param("userId") Long userId, @Param("status") BookingStatus status);
    
    @Query("SELECT dc FROM DatCho dc WHERE dc.chargingPoint.id = :chargerId AND dc.bookingTime BETWEEN :startTime AND :endTime AND dc.status != 'CANCELLED'")
    List<DatCho> findConflictingBookings(@Param("chargerId") Long chargerId, 
                                         @Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT dc FROM DatCho dc WHERE dc.bookingTime < :expiryTime AND dc.status = 'PENDING'")
    List<DatCho> findExpiredBookings(@Param("expiryTime") LocalDateTime expiryTime);
}
