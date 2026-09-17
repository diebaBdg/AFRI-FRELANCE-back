package AfriFreelance.API.business.profile.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkExperienceRequest {

    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 200)
    private String jobTitle;

    @NotBlank(message = "L'entreprise est obligatoire")
    @Size(max = 200)
    private String company;

    private String country;

    @jakarta.validation.constraints.NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean current;

    @Size(max = 2000)
    private String description;
}
