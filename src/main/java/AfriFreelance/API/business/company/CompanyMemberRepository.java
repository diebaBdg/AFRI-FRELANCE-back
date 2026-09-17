package AfriFreelance.API.business.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {
    List<CompanyMember> findByCompanyId(UUID companyId);
    List<CompanyMember> findByUserId(UUID userId);
    Optional<CompanyMember> findByCompanyIdAndUserId(UUID companyId, UUID userId);
}
