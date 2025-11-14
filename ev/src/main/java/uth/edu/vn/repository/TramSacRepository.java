package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.TramSac;
import uth.edu.vn.enums.StationStatus;

import java.util.List;

@Repository
public interface TramSacRepository extends JpaRepository<TramSac, Long> {
    
    List<TramSac> findByStatus(String status);
    
    /**
     * Find stations by status enum (for EVDriverService)
     */
    @Query("SELECT t FROM TramSac t WHERE t.status = :status")
    List<TramSac> findByStatus(@Param("status") StationStatus status);
    
    @Query("SELECT t FROM TramSac t WHERE t.status = 'ONLINE'")
    List<TramSac> findAllOnlineStations();
    
    @Query(value = "SELECT *, " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
           "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(latitude)))) AS distance " +
           "FROM tram_sac " +
           "HAVING distance < :radius " +
           "ORDER BY distance", nativeQuery = true)
    List<TramSac> findNearbyStations(@Param("latitude") Double latitude, 
                                     @Param("longitude") Double longitude, 
                                     @Param("radius") Double radius);
    
    @Query("SELECT t FROM TramSac t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TramSac> searchStations(@Param("keyword") String keyword);
}
