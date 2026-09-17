package AfriFreelance.API.business.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkPreferenceRepository extends JpaRepository<WorkPreference, UUID> {
    Optional<WorkPreference> findByFreelanceProfileId(UUID freelanceProfileId);
}
