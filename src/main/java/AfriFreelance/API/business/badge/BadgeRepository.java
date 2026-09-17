package AfriFreelance.API.business.badge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    List<Badge> findByUserId(UUID userId);
    Optional<Badge> findByUserIdAndBadgeType(UUID userId, AfriFreelance.API.enums.BadgeType badgeType);
}
