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
public class FreelanceProfileDTO {
    private UUID id;
    private UUID userId;
    private String title;
    private String overview;
    private String country;
    private String city;
    private Double hourlyRate;
    private Double dailyRate;
    private Double minProjectRate;
    private String currency;
    private String availabilityStatus;
    private String availabilityType;
    private Integer weeklyHours;
    private Boolean profileActive;
    private Integer completionPercentage;
    private Boolean isVerified;
    private java.util.List<SkillDTO> skills;
    private java.util.List<WorkExperienceDTO> experiences;
    private java.util.List<EducationDTO> educations;
    private java.util.List<CertificationDTO> certifications;
    private java.util.List<LanguageDTO> languages;
    private java.util.List<PortfolioItemDTO> portfolioItems;
    private java.util.List<ProfessionalLinkDTO> professionalLinks;
    private WorkPreferenceDTO workPreference;
}
