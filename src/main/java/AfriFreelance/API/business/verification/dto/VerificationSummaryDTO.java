package AfriFreelance.API.business.verification.dto;

import AfriFreelance.API.business.profile.dto.BadgeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSummaryDTO {
    private List<VerificationDTO> verifications;
    private int completed;
    private int total;
    private double progressPercentage;
}
