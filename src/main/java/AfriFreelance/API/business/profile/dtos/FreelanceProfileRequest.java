package AfriFreelance.API.business.profile.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FreelanceProfileRequest {

    @NotBlank(message = "Le titre professionnel est obligatoire")
    @Size(max = 200)
    private String title;

    @Size(max = 5000, message = "La présentation ne doit pas dépasser 5000 caractères")
    private String overview;

    private String country;
    private String city;

    private Double hourlyRate;
    private Double dailyRate;
    private Double minProjectRate;
    private String currency;

    private String availabilityStatus;
    private String availabilityType;
    private Integer weeklyHours;
    private Boolean profileActive;
}
