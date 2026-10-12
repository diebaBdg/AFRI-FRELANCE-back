package AfriFreelance.API.business.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Page<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status, Pageable pageable);

    Page<Project> findByClientIdOrderByCreatedAtDesc(UUID clientId, Pageable pageable);

    List<Project> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    @Query("SELECT p FROM Project p WHERE p.status = :status AND " +
           "(:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:country IS NULL OR p.country = :country) " +
           "ORDER BY p.createdAt DESC")
    Page<Project> searchProjects(@Param("status") ProjectStatus status,
                                  @Param("query") String query,
                                  @Param("category") String category,
                                  @Param("country") String country,
                                  Pageable pageable);
}
