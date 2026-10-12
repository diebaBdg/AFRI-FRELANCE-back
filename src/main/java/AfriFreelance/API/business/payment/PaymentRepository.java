package AfriFreelance.API.business.payment;

import AfriFreelance.API.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByContractIdOrderByCreatedAtDesc(UUID contractId);

    List<Payment> findByPayerIdOrderByCreatedAtDesc(UUID payerId);

    List<Payment> findByPayeeIdOrderByCreatedAtDesc(UUID payeeId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByTransactionRef(String transactionRef);

    Optional<Payment> findByContractIdAndPaymentType(UUID contractId, AfriFreelance.API.enums.PaymentType paymentType);
}
