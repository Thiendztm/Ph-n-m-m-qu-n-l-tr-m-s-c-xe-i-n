package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.Charger;
import uth.edu.vn.enums.ConnectorType;
import uth.edu.vn.enums.PointStatus;

import java.util.List;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {
    
    List<Charger> findByChargingStationId(Long stationId);
    
    List<Charger> findByStatus(PointStatus status);
    
    @Query("SELECT c FROM Charger c WHERE c.chargingStation.id = :stationId AND c.status = :status")
    List<Charger> findByStationAndStatus(@Param("stationId") Long stationId, @Param("status") PointStatus status);
    
    @Query("SELECT c FROM Charger c WHERE c.connectorType = :connectorType AND c.status = 'AVAILABLE'")
    List<Charger> findAvailableChargersByType(@Param("connectorType") ConnectorType connectorType);
    
    @Query("SELECT COUNT(c) FROM Charger c WHERE c.chargingStation.id = :stationId AND c.status = 'AVAILABLE'")
    Long countAvailableChargersByStation(@Param("stationId") Long stationId);
    
    /**
     * Count charging points by status (for AdminService system overview)
     */
    Long countByStatus(PointStatus status);
    
    /**
     * Find charging points by station and status (for EVDriverService)
     */
    List<Charger> findByChargingStationIdAndStatus(Long stationId, PointStatus status);
}
