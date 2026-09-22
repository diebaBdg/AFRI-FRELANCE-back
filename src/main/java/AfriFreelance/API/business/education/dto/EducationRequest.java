package AfriFreelance.API.business.education.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EducationRequest {

    @NotBlank(message = "L'établissement est obligatoire")
    @Size(max = 200)
    private String institution;

    @NotBlank(message = "Le diplôme est obligatoire")
    @Size(max = 200)
    private String degree;

    @Size(max = 200)
    private String field;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 2000)
    private String description;
}
