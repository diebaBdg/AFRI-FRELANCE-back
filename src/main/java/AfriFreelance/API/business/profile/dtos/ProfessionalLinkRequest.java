package AfriFreelance.API.business.profile.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfessionalLinkRequest {

    @NotBlank(message = "La plateforme est obligatoire")
    private String platform;

    @NotBlank(message = "L'URL est obligatoire")
    @Size(max = 500)
    private String url;
}
