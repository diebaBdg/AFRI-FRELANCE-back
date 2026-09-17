package AfriFreelance.API.business.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatDTO {
    private String key;
    private String label;
    private String value;
    private String unit;
}
