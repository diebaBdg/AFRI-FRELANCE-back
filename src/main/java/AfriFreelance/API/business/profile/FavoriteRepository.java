package AfriFreelance.API.business.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserId(UUID userId);
    Optional<Favorite> findByUserIdAndTargetUserId(UUID userId, UUID targetUserId);
    boolean existsByUserIdAndTargetUserId(UUID userId, UUID targetUserId);
}
