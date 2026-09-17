package AfriFreelance.API.business.profile.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactRequest {

    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, max = 2000, message = "Le message doit contenir entre 10 et 2000 caractères")
    private String message;

    @Size(max = 200, message = "Le sujet ne doit pas dépasser 200 caractères")
    private String subject;
}
