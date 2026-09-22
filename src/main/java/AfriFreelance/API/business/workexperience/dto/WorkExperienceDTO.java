package AfriFreelance.API.business.workexperience.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceDTO {
    private UUID id;
    private String jobTitle;
    private String company;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean current;
    private String description;
}
