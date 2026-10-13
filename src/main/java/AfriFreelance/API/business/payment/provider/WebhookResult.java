package AfriFreelance.API.business.payment.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResult {
    private String transactionRef;
    private String providerTransactionId;
    private boolean success;
    private double amount;
}
