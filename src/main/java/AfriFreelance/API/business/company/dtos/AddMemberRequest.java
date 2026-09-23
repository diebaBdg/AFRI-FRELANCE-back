package AfriFreelance.API.business.company.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotBlank(message = "L'email ou le nom d'utilisateur est obligatoire")
    private String identifier;
    private String role;
}
