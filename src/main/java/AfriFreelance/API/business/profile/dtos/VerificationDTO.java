package AfriFreelance.API.business.profile.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDTO {
    private UUID id;
    private String verificationType;
    private String status;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
