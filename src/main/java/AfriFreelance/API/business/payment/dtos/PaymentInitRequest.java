package AfriFreelance.API.business.payment.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PaymentInitRequest {

    @NotNull(message = "L'ID du contrat est obligatoire")
    private UUID contractId;

    @NotBlank(message = "Le fournisseur de paiement est obligatoire (WAVE ou ORANGE_MONEY)")
    private String providerName;

    private String customerPhone;

    private String successUrl;

    private String errorUrl;
}
