package AfriFreelance.API.business.profile;

import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.business.review.ReviewRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.BadgeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PublicProfileService {

    private final UserRepository userRepository;
    private final FreelanceProfileRepository freelanceProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final BadgeRepository badgeRepository;
    private final VerificationService verificationService;
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ReviewRepository reviewRepository;

    public PublicFreelanceProfileDTO getPublicFreelanceProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé: " + username, username));

        FreelanceProfile fp = freelanceProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("NO_FREELANCE_PROFILE",
                        "Cet utilisateur n'a pas de profil freelance", username));

        if (!Boolean.TRUE.equals(fp.getProfileActive())) {
            throw new BusinessException("PROFILE_INACTIVE", "Ce profil n'est pas disponible", username);
        }

        List<BadgeDTO> badges = verificationService.getBadgesForUser(user.getId());

        return PublicFreelanceProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .title(fp.getTitle())
                .overview(fp.getOverview())
                .country(fp.getCountry() != null ? fp.getCountry() : user.getCountry())
                .city(fp.getCity())
                .availabilityStatus(fp.getAvailabilityStatus() != null ? fp.getAvailabilityStatus().name() : null)
                .availabilityType(fp.getAvailabilityType() != null ? fp.getAvailabilityType().name() : null)
                .weeklyHours(fp.getWeeklyHours())
                .hourlyRate(fp.getHourlyRate())
                .dailyRate(fp.getDailyRate())
                .currency(fp.getCurrency() != null ? fp.getCurrency().name() : "XOF")
                .isVerified(fp.getIsVerified())
                .memberSinceYear(user.getMemberSinceYear())
                .avgRating(reviewRepository.getAverageRating(user.getId()))
                .totalReviews(reviewRepository.getReviewCount(user.getId()).intValue())
                .skills(fp.getSkills().stream()
                        .map(s -> SkillDTO.builder().id(s.getId()).name(s.getName()).level(s.getLevel().name()).build())
                        .collect(Collectors.toList()))
                .experiences(fp.getExperiences().stream()
                        .map(e -> WorkExperienceDTO.builder()
                                .id(e.getId()).jobTitle(e.getJobTitle()).company(e.getCompany())
                                .country(e.getCountry()).startDate(e.getStartDate()).endDate(e.getEndDate())
                                .current(e.getCurrent()).description(e.getDescription()).build())
                        .collect(Collectors.toList()))
                .educations(fp.getEducations().stream()
                        .map(e -> EducationDTO.builder()
                                .id(e.getId()).institution(e.getInstitution()).degree(e.getDegree())
                                .field(e.getField()).startDate(e.getStartDate()).endDate(e.getEndDate())
                                .description(e.getDescription()).build())
                        .collect(Collectors.toList()))
                .certifications(fp.getCertifications().stream()
                        .map(c -> CertificationDTO.builder()
                                .id(c.getId()).name(c.getName()).issuer(c.getIssuer())
                                .issueDate(c.getIssueDate()).verificationUrl(c.getVerificationUrl())
                                .credentialId(c.getCredentialId()).build())
                        .collect(Collectors.toList()))
                .languages(fp.getLanguages().stream()
                        .map(l -> LanguageDTO.builder().id(l.getId()).languageName(l.getLanguageName()).level(l.getLevel().name()).build())
                        .collect(Collectors.toList()))
                .portfolioItems(fp.getPortfolioItems().stream()
                        .map(p -> PortfolioItemDTO.builder()
                                .id(p.getId()).title(p.getTitle()).description(p.getDescription())
                                .imageUrl(p.getImageUrl()).projectUrl(p.getProjectUrl())
                                .technologies(p.getTechnologies()).projectDate(p.getProjectDate()).role(p.getRole()).build())
                        .collect(Collectors.toList()))
                .professionalLinks(fp.getProfessionalLinks().stream()
                        .map(l -> ProfessionalLinkDTO.builder().id(l.getId()).platform(l.getPlatform().name()).url(l.getUrl()).build())
                        .collect(Collectors.toList()))
                .workPreference(fp.getWorkPreference() != null ? WorkPreferenceDTO.builder()
                        .id(fp.getWorkPreference().getId())
                        .workTypes(fp.getWorkPreference().getWorkTypes())
                        .projectTypes(fp.getWorkPreference().getProjectTypes())
                        .acceptedCountries(fp.getWorkPreference().getAcceptedCountries())
                        .timezone(fp.getWorkPreference().getTimezone())
                        .build() : null)
                .badges(badges)
                .build();
    }

    public PublicClientProfileDTO getPublicClientProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé: " + username, username));

        ClientProfile cp = clientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("NO_CLIENT_PROFILE",
                        "Cet utilisateur n'a pas de profil client", username));

        if (!Boolean.TRUE.equals(cp.getProfileActive())) {
            throw new BusinessException("PROFILE_INACTIVE", "Ce profil n'est pas disponible", username);
        }

        List<BadgeDTO> badges = verificationService.getBadgesForUser(user.getId());

        CompanyDTO companyDTO = null;
        if (cp.getCompanyId() != null) {
            companyDTO = companyRepository.findById(cp.getCompanyId())
                    .map(c -> CompanyDTO.builder()
                            .id(c.getId())
                            .legalName(c.getLegalName())
                            .commercialName(c.getCommercialName())
                            .sector(c.getSector())
                            .size(c.getSize() != null ? c.getSize().name() : "SOLO")
                            .country(c.getCountry())
                            .city(c.getCity())
                            .website(c.getWebsite())
                            .description(c.getDescription())
                            .logoUrl(c.getLogoUrl())
                            .isVerified(c.getIsVerified())
                            .createdAt(c.getCreatedAt())
                            .members(List.of())
                            .build())
                    .orElse(null);
        }

        return PublicClientProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .displayName(cp.getDisplayName() != null ? cp.getDisplayName() : user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .country(cp.getCountry() != null ? cp.getCountry() : user.getCountry())
                .city(cp.getCity())
                .memberSinceYear(user.getMemberSinceYear())
                .projectsPublished(cp.getProjectsPublished())
                .projectsCompleted(cp.getProjectsCompleted())
                .hireRate(cp.getHireRate())
                .avgRating(cp.getAvgRating())
                .totalSpent(cp.getTotalSpent())
                .clientType(cp.getClientType() != null ? cp.getClientType().name() : "INDIVIDUAL")
                .company(companyDTO)
                .badges(badges)
                .build();
    }

    public List<FreelancerCardDTO> searchFreelancers(String query, String country, int limit) {
        List<User> users = userRepository.findAll();

        return users.stream()
                .filter(u -> {
                    FreelanceProfile fp = freelanceProfileRepository.findByUserId(u.getId()).orElse(null);
                    if (fp == null || !Boolean.TRUE.equals(fp.getProfileActive())) return false;
                    if (country != null && !country.isBlank() && !country.equalsIgnoreCase(fp.getCountry())) return false;
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        boolean matchTitle = fp.getTitle() != null && fp.getTitle().toLowerCase().contains(q);
                        boolean matchName = u.getFullName() != null && u.getFullName().toLowerCase().contains(q);
                        boolean matchSkills = fp.getSkills().stream()
                                .anyMatch(s -> s.getName().toLowerCase().contains(q));
                        if (!matchTitle && !matchName && !matchSkills) return false;
                    }
                    return true;
                })
                .limit(limit)
                .map(u -> {
                    FreelanceProfile fp = freelanceProfileRepository.findByUserId(u.getId()).orElse(null);
                    List<BadgeDTO> badges = verificationService.getBadgesForUser(u.getId());
                    return FreelancerCardDTO.builder()
                            .userId(u.getId())
                            .username(u.getUsername())
                            .fullName(u.getFullName())
                            .avatarUrl(u.getAvatarUrl())
                            .title(fp != null ? fp.getTitle() : null)
                            .country(fp != null ? fp.getCountry() : u.getCountry())
                            .city(fp != null ? fp.getCity() : null)
                            .hourlyRate(fp != null ? fp.getHourlyRate() : null)
                            .currency(fp != null && fp.getCurrency() != null ? fp.getCurrency().name() : "XOF")
                            .availabilityStatus(fp != null && fp.getAvailabilityStatus() != null ? fp.getAvailabilityStatus().name() : null)
                            .isVerified(fp != null ? fp.getIsVerified() : false)
                            .completionPercentage(fp != null ? fp.getCompletionPercentage() : 0)
                            .topSkills(fp != null ? fp.getSkills().stream()
                                    .limit(5)
                                    .map(Skill::getName)
                                    .collect(Collectors.toList()) : List.of())
                            .badges(badges)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public UUID getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé: " + username, username))
                .getId();
    }
}
