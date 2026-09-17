package AfriFreelance.API.business.profile.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LanguageRequest {

    @NotBlank(message = "Le nom de la langue est obligatoire")
    @Size(max = 100)
    private String languageName;

    private String level;
}
