package AfriFreelance.API.business.profile.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItemDTO {
    private UUID id;
    private String title;
    private String description;
    private String imageUrl;
    private String projectUrl;
    private String[] technologies;
    private LocalDate projectDate;
    private String role;
}
