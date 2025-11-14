package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.Charger;
import uth.edu.vn.entity.DieuKhienTuXa;
import uth.edu.vn.entity.Xe;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DieuKhienTuXaRepository extends JpaRepository<DieuKhienTuXa, Long> {
    
    /**
     * Find telemetry by charging point
     */
    List<DieuKhienTuXa> findByChargingPoint(Charger chargingPoint);
    
    /**
     * Find telemetry by charging point ID ordered by recorded time descending
     */
    List<DieuKhienTuXa> findByChargingPointPointIdOrderByRecordedAtDesc(Long pointId);
    
    /**
     * Find telemetry by vehicle
     */
    List<DieuKhienTuXa> findByVehicle(Xe vehicle);
    
    /**
     * Find latest telemetry for charging point
     */
    @Query("SELECT t FROM DieuKhienTuXa t " +
           "WHERE t.chargingPoint.pointId = :pointId " +
           "ORDER BY t.recordedAt DESC " +
           "LIMIT 1")
    Optional<DieuKhienTuXa> findLatestByChargingPoint(@Param("pointId") Long pointId);
    
    /**
     * Find telemetry by date range
     */
    @Query("SELECT t FROM DieuKhienTuXa t " +
           "WHERE t.recordedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.recordedAt DESC")
    List<DieuKhienTuXa> findByRecordedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find telemetry by charging point and date range
     */
    @Query("SELECT t FROM DieuKhienTuXa t " +
           "WHERE t.chargingPoint.pointId = :pointId " +
           "AND t.recordedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.recordedAt ASC")
    List<DieuKhienTuXa> findByChargingPointAndDateRange(@Param("pointId") Long pointId, 
                                                        @Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find telemetry with errors
     */
    @Query("SELECT t FROM DieuKhienTuXa t " +
           "WHERE t.errorCode IS NOT NULL " +
           "ORDER BY t.recordedAt DESC")
    List<DieuKhienTuXa> findTelemetryWithErrors();
    
    /**
     * Find telemetry with errors by charging point
     */
    @Query("SELECT t FROM DieuKhienTuXa t " +
           "WHERE t.chargingPoint.pointId = :pointId " +
           "AND t.errorCode IS NOT NULL " +
           "ORDER BY t.recordedAt DESC")
    List<DieuKhienTuXa> findErrorsByChargingPoint(@Param("pointId") Long pointId);
    
    /**
     * Get average power for charging point in time range
     */
    @Query("SELECT AVG(t.currentPowerKw) FROM DieuKhienTuXa t " +
           "WHERE t.chargingPoint.pointId = :pointId " +
           "AND t.recordedAt BETWEEN :startDate AND :endDate")
    Double getAveragePowerByChargingPoint(@Param("pointId") Long pointId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total energy delivered by charging point
     */
    @Query("SELECT COALESCE(SUM(t.energyDeliveredKwh), 0.0) FROM DieuKhienTuXa t " +
           "WHERE t.chargingPoint.pointId = :pointId " +
           "AND t.recordedAt BETWEEN :startDate AND :endDate")
    Double getTotalEnergyByChargingPoint(@Param("pointId") Long pointId, 
                                        @Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find high temperature readings (warning threshold)
     */
    @Query("SELECT t FROM DieuKhienTuXa t " +
           "WHERE t.temperatureC > :threshold " +
           "ORDER BY t.temperatureC DESC, t.recordedAt DESC")
    List<DieuKhienTuXa> findHighTemperatureReadings(@Param("threshold") Double threshold);
    
    /**
     * Find recent telemetry for vehicle
     */
    @Query("SELECT t FROM DieuKhienTuXa t " +
           "WHERE t.vehicle.id = :vehicleId " +
           "ORDER BY t.recordedAt DESC")
    List<DieuKhienTuXa> findRecentByVehicle(@Param("vehicleId") Long vehicleId, 
                                           org.springframework.data.domain.Pageable pageable);
    
    /**
     * Count telemetry records for charging point
     */
    Long countByChargingPointPointId(Long pointId);
    
    /**
     * Delete old telemetry data (cleanup)
     */
    @Query("DELETE FROM DieuKhienTuXa t WHERE t.recordedAt < :cutoffDate")
    void deleteOldTelemetry(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find telemetry by charging point in last N hours
     */
    @Query("SELECT t FROM DieuKhienTuXa t " +
           "WHERE t.chargingPoint.pointId = :pointId " +
           "AND t.recordedAt > :sinceTime " +
           "ORDER BY t.recordedAt DESC")
    List<DieuKhienTuXa> findRecentByChargingPoint(@Param("pointId") Long pointId, 
                                                  @Param("sinceTime") LocalDateTime sinceTime);
    
    /**
     * Get charging statistics for station
     */
    @Query("SELECT t.chargingPoint.pointId, " +
           "COUNT(t), " +
           "AVG(t.currentPowerKw), " +
           "SUM(t.energyDeliveredKwh), " +
           "AVG(t.temperatureC) " +
           "FROM DieuKhienTuXa t " +
           "WHERE t.chargingPoint.chargingStation.id = :stationId " +
           "AND t.recordedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY t.chargingPoint.pointId")
    List<Object[]> getStationStatistics(@Param("stationId") Long stationId, 
                                       @Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
}
