package AfriFreelance.API.business.moderation.dtos;

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
public class ReportDTO {
    private UUID id;
    private UUID reporterId;
    private String reporterName;
    private String reportType;
    private UUID targetId;
    private String reason;
    private String description;
    private String status;
    private UUID resolvedById;
    private String resolvedByName;
    private String resolutionNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
