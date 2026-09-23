package AfriFreelance.API.business.clientprofile.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientProfileRequest {

    @NotBlank(message = "Le nom affiché est obligatoire")
    @Size(max = 200)
    private String displayName;

    private String clientType;

    @Size(max = 2000)
    private String overview;

    private String country;
    private String city;
    private Boolean profileActive;
}
