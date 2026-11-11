package AfriFreelance.API.business.payment.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentSoftPayRequest {

    @NotBlank(message = "Le canal est obligatoire (wave-senegal, orange-money-senegal, djamo)")
    private String channel;

    private String customerPhone;
}
