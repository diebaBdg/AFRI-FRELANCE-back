package AfriFreelance.API.business.profile.dtos;

import AfriFreelance.API.business.badge.dtos.BadgeDTO;
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
public class ProfileSummaryDTO {
    private UUID userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String headline;
    private String country;
    private Integer memberSinceYear;
    private String activeMode;
    private Boolean hasFreelanceProfile;
    private Boolean hasClientProfile;
    private Boolean freelanceProfileActive;
    private Boolean clientProfileActive;
    private Integer freelanceCompletion;
    private Integer clientCompletion;
    private List<BadgeDTO> badges;
    private List<String> availableModes;
}
