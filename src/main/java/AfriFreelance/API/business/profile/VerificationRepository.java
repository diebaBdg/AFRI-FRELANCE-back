package AfriFreelance.API.business.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {
    List<Verification> findByUserId(UUID userId);
    Optional<Verification> findByUserIdAndVerificationType(UUID userId, AfriFreelance.API.enums.VerificationType type);
}
