package AfriFreelance.API.business.contract.dtos;

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
public class DeliveryDTO {
    private UUID id;
    private UUID contractId;
    private String description;
    private String attachmentUrl;
    private String status;
    private String clientFeedback;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}
