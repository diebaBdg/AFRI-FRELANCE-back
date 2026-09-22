package AfriFreelance.API.business.certification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDTO {
    private UUID id;
    private String name;
    private String issuer;
    private LocalDate issueDate;
    private String verificationUrl;
    private String credentialId;
}
