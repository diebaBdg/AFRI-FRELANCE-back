package AfriFreelance.API.business.payment.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private UUID id;
    private String transactionRef;
    private UUID contractId;
    private UUID payerId;
    private String payerName;
    private UUID payeeId;
    private String payeeName;
    private String paymentType;
    private Double amount;
    private String currency;
    private Double commissionAmount;
    private Double netAmount;
    private String status;
    private String providerName;
    private String providerTransactionId;
    private LocalDateTime paidAt;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
}
