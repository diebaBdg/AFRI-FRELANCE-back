package AfriFreelance.API.business.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    List<Contract> findByFreelanceIdOrderByCreatedAtDesc(UUID freelanceId);
    List<Contract> findByClientIdOrFreelanceIdOrderByCreatedAtDesc(UUID clientId, UUID freelanceId);
}
