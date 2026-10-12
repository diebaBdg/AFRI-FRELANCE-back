package AfriFreelance.API.business.proposal.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProposalRequest {

    @NotNull(message = "Le montant proposé est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private Double proposedAmount;

    private String currency;

    private Integer estimatedDurationDays;

    @NotBlank(message = "La lettre de motivation est obligatoire")
    @Size(max = 5000, message = "La lettre ne doit pas dépasser 5000 caractères")
    private String coverLetter;

    private String attachmentUrl;
}
