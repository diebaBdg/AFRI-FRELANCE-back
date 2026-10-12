package AfriFreelance.API.business.contract.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {
    private UUID id;
    private UUID proposalId;
    private UUID projectId;
    private String projectTitle;
    private UUID clientId;
    private String clientName;
    private UUID freelanceId;
    private String freelanceName;
    private Double amount;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer estimatedDays;
    private String status;
    private Boolean isPaid;
    private Double commissionRate;
    private Double commissionAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeliveryDTO> deliveries;
}
