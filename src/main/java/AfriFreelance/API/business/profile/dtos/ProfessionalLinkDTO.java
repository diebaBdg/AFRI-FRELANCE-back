package AfriFreelance.API.business.profile.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalLinkDTO {
    private UUID id;
    private String platform;
    private String url;
}
