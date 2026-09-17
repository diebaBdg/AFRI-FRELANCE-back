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
public class ClientProfileDTO {
    private UUID id;
    private UUID userId;
    private String displayName;
    private String clientType;
    private UUID companyId;
    private String overview;
    private String country;
    private String city;
    private Boolean profileActive;
    private Integer projectsPublished;
    private Integer projectsCompleted;
    private Double hireRate;
    private Double avgRating;
    private Double totalSpent;
}
