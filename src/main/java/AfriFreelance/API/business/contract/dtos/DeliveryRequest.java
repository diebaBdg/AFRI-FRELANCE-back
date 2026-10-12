package AfriFreelance.API.business.contract.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliveryRequest {

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 5000)
    private String description;

    private String attachmentUrl;
}
