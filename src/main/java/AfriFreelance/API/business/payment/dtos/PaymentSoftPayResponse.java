package AfriFreelance.API.business.payment.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSoftPayResponse {
    private boolean success;
    private String message;
    private String url;
    private String omUrl;
    private String maxitUrl;
    private PaymentDTO payment;
}
