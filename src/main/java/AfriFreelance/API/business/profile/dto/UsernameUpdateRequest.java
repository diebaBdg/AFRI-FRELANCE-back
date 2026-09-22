package AfriFreelance.API.business.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsernameUpdateRequest {
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscore")
    private String username;
}
