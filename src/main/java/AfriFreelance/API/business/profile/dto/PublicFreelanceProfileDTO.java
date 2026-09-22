package AfriFreelance.API.business.profile.dto;

import AfriFreelance.API.business.certification.dto.CertificationDTO;
import AfriFreelance.API.business.education.dto.EducationDTO;
import AfriFreelance.API.business.language.dto.LanguageDTO;
import AfriFreelance.API.business.portfolio.dto.PortfolioItemDTO;
import AfriFreelance.API.business.professionallink.dto.ProfessionalLinkDTO;
import AfriFreelance.API.business.skill.dto.SkillDTO;
import AfriFreelance.API.business.workexperience.dto.WorkExperienceDTO;
import AfriFreelance.API.business.workpreference.dto.WorkPreferenceDTO;
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
public class PublicFreelanceProfileDTO {
    private UUID userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String title;
    private String overview;
    private String country;
    private String city;
    private String availabilityStatus;
    private String availabilityType;
    private Integer weeklyHours;
    private Double hourlyRate;
    private Double dailyRate;
    private String currency;
    private Boolean isVerified;
    private Integer memberSinceYear;
    private Double avgRating;
    private Integer totalReviews;
    private List<SkillDTO> skills;
    private List<WorkExperienceDTO> experiences;
    private List<EducationDTO> educations;
    private List<CertificationDTO> certifications;
    private List<LanguageDTO> languages;
    private List<PortfolioItemDTO> portfolioItems;
    private List<ProfessionalLinkDTO> professionalLinks;
    private WorkPreferenceDTO workPreference;
    private List<BadgeDTO> badges;
}
