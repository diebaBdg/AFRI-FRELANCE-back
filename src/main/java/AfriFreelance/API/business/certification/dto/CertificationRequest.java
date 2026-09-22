package AfriFreelance.API.business.certification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CertificationRequest {

    @NotBlank(message = "Le nom de la certification est obligatoire")
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String issuer;

    private LocalDate issueDate;

    @Size(max = 500)
    private String verificationUrl;

    @Size(max = 200)
    private String credentialId;
}
