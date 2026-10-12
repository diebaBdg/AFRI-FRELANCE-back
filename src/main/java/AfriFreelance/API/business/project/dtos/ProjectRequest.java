package AfriFreelance.API.business.project.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 10000)
    private String description;

    private String projectType;

    private Double budgetMin;
    private Double budgetMax;
    private String currency;

    private Integer estimatedDurationDays;
    private LocalDate expectedStartDate;

    @Size(max = 100)
    private String category;

    private String[] skillsRequired;
    private String country;
    private Boolean isRemote;
}
