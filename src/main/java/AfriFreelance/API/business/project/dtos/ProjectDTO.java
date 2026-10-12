package AfriFreelance.API.business.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private String clientAvatarUrl;
    private String title;
    private String description;
    private String projectType;
    private Double budgetMin;
    private Double budgetMax;
    private String currency;
    private Integer estimatedDurationDays;
    private LocalDate expectedStartDate;
    private String category;
    private String[] skillsRequired;
    private String country;
    private Boolean isRemote;
    private String status;
    private Integer proposalCount;
    private UUID assignedFreelanceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
