package AfriFreelance.API.business.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private String mode;
    private String fullName;
    private String avatarUrl;
    private String headline;
    private Integer completionPercentage;
    private Boolean isVerified;
    private Integer badgeCount;
    private Integer favoriteCount;
    private List<DashboardStatDTO> stats;
}
