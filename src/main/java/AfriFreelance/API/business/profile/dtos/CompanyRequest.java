package AfriFreelance.API.business.profile.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRequest {

    @NotBlank(message = "Le nom commercial est obligatoire")
    @Size(max = 200)
    private String commercialName;

    @Size(max = 200)
    private String legalName;

    @Size(max = 100)
    private String sector;

    private String size;

    private String country;
    private String city;

    @Size(max = 500)
    private String website;

    @Size(max = 5000)
    private String description;

    private String logoUrl;
}
