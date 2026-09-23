package AfriFreelance.API.business.portfolio.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PortfolioItemRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    private String imageUrl;

    @Size(max = 500)
    private String projectUrl;

    private String[] technologies;

    private LocalDate projectDate;

    @Size(max = 200)
    private String role;
}
