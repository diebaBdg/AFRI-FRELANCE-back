package AfriFreelance.API.business.payment.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentInitRequest {

    @NotNull(message = "L'ID du contrat est obligatoire")
    private UUID contractId;

    @NotBlank(message = "Le fournisseur de paiement est obligatoire")
    private String providerName;

    @NotBlank(message = "Le numéro de téléphone ou moyen de paiement est obligatoire")
    private String paymentMethod;
}
