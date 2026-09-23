package AfriFreelance.API.business.education.dtos;

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
public class EducationDTO {
    private UUID id;
    private String institution;
    private String degree;
    private String field;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
