package uth.edu.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uth.edu.vn.entity.Xe;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Xe (Vehicle) entity
 * Spring Data JPA automatically implements basic CRUD operations
 */
@Repository
public interface XeRepository extends JpaRepository<Xe, Long> {
    
    /**
     * Find vehicle by plate number
     */
    Optional<Xe> findByPlateNumber(String plateNumber);
    
    /**
     * Find all vehicles owned by a user
     */
    List<Xe> findByUserId(Long userId);
    
    /**
     * Find the first vehicle owned by a user (for profile)
     */
    Optional<Xe> findFirstByUserId(Long userId);
    
    /**
     * Check if vehicle exists by plate number
     */
    boolean existsByPlateNumber(String plateNumber);
}
