package AfriFreelance.API.business.freelanceprofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FreelanceProfileRepository extends JpaRepository<FreelanceProfile, UUID> {
    Optional<FreelanceProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
