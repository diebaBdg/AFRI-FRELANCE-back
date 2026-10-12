package AfriFreelance.API.business.moderation.dtos;

import AfriFreelance.API.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportResolutionRequest {

    @NotNull(message = "Le statut de résolution est obligatoire")
    private ReportStatus status;

    private String resolutionNote;
}
