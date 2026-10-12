package AfriFreelance.API.business.moderation.dtos;

import AfriFreelance.API.enums.ReportReason;
import AfriFreelance.API.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ReportRequest {

    @NotNull(message = "Le type de signalement est obligatoire")
    private ReportType reportType;

    @NotNull(message = "La cible du signalement est obligatoire")
    private UUID targetId;

    @NotNull(message = "La raison du signalement est obligatoire")
    private ReportReason reason;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;
}
