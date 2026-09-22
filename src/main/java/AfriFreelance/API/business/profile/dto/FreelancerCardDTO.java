package AfriFreelance.API.business.profile.dto;

import AfriFreelance.API.business.skill.dto.SkillDTO;
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
public class FreelancerCardDTO {
    private UUID userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String title;
    private String country;
    private String city;
    private Double hourlyRate;
    private String currency;
    private String availabilityStatus;
    private Boolean isVerified;
    private Integer completionPercentage;
    private List<String> topSkills;
    private List<BadgeDTO> badges;
}
