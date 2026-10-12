package AfriFreelance.API.business.admin.dtos;

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
public class AdminProjectDTO {
    private UUID id;
    private String title;
    private String status;
    private String category;
    private UUID clientId;
    private String clientName;
    private Double budgetMin;
    private Double budgetMax;
    private String currency;
    private Integer proposalCount;
    private UUID assignedFreelanceId;
    private String assignedFreelanceName;
    private LocalDateTime createdAt;
}
