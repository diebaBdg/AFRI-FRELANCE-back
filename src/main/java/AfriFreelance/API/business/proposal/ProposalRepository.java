package AfriFreelance.API.business.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, UUID> {
    List<Proposal> findByProjectIdOrderByCreatedAtAsc(UUID projectId);
    List<Proposal> findByFreelanceIdOrderByCreatedAtDesc(UUID freelanceId);
    Optional<Proposal> findByProjectIdAndFreelanceId(UUID projectId, UUID freelanceId);
    boolean existsByProjectIdAndFreelanceId(UUID projectId, UUID freelanceId);
}
