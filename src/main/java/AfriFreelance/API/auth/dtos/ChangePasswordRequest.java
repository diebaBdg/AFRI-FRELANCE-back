package AfriFreelance.API.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import AfriFreelance.API.validation.StrongPassword;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    private String oldPassword;
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @StrongPassword
    private String newPassword;
}
