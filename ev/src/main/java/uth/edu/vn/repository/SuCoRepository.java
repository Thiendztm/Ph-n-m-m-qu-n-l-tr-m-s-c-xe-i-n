package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.SuCo;
import uth.edu.vn.enums.IncidentStatus;

import java.util.List;

@Repository
public interface SuCoRepository extends JpaRepository<SuCo, Long> {
    
    List<SuCo> findByStatus(IncidentStatus status);
    
    List<SuCo> findByChargingPointId(Long chargerId);
    
    @Query("SELECT sc FROM SuCo sc WHERE sc.reportedBy.id = :userId ORDER BY sc.reportedAt DESC")
    List<SuCo> findByReportedByOrderByReportedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT sc FROM SuCo sc WHERE sc.status != 'RESOLVED' ORDER BY sc.severity DESC, sc.reportedAt ASC")
    List<SuCo> findUnresolvedIncidents();
}
