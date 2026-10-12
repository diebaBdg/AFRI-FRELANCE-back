package AfriFreelance.API.business.proposal.dtos;

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
public class ProposalDTO {
    private UUID id;
    private UUID projectId;
    private String projectTitle;
    private UUID freelanceId;
    private String freelanceName;
    private String freelanceAvatarUrl;
    private String freelanceHeadline;
    private Double proposedAmount;
    private String currency;
    private Integer estimatedDurationDays;
    private String coverLetter;
    private String attachmentUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
