package AfriFreelance.API.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sn.gainde2000.senegallicenseplatformbackend.validation.StrongPassword;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @StrongPassword
    private String newPassword;

    @NotBlank
    @Size(min = 6)
    private String confirmPassword;
}
