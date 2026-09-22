package AfriFreelance.API.business.profile.dto;

import AfriFreelance.API.business.company.dto.CompanyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicClientProfileDTO {
    private UUID userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String country;
    private String city;
    private Integer memberSinceYear;
    private Integer projectsPublished;
    private Integer projectsCompleted;
    private Double hireRate;
    private Double avgRating;
    private Double totalSpent;
    private String clientType;
    private CompanyDTO company;
    private List<BadgeDTO> badges;
}
