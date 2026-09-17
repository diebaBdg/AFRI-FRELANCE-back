package AfriFreelance.API.business.freelanceprofile;

import AfriFreelance.API.business.certification.Certification;
import AfriFreelance.API.business.certification.CertificationRepository;
import AfriFreelance.API.business.clientprofile.ClientProfileRepository;
import AfriFreelance.API.business.education.Education;
import AfriFreelance.API.business.education.EducationRepository;
import AfriFreelance.API.business.language.Language;
import AfriFreelance.API.business.language.LanguageRepository;
import AfriFreelance.API.business.portfolio.PortfolioItem;
import AfriFreelance.API.business.portfolio.PortfolioItemRepository;
import AfriFreelance.API.business.professionallink.ProfessionalLink;
import AfriFreelance.API.business.professionallink.ProfessionalLinkRepository;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.skill.Skill;
import AfriFreelance.API.business.skill.SkillRepository;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.business.workexperience.WorkExperience;
import AfriFreelance.API.business.workexperience.WorkExperienceRepository;
import AfriFreelance.API.business.workpreference.WorkPreference;
import AfriFreelance.API.business.workpreference.WorkPreferenceRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FreelanceProfileService {

    private final FreelanceProfileRepository freelanceProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationRepository educationRepository;
    private final CertificationRepository certificationRepository;
    private final LanguageRepository languageRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final ProfessionalLinkRepository professionalLinkRepository;
    private final WorkPreferenceRepository workPreferenceRepository;
    private final ProfileService profileService;

    public FreelanceProfileDTO getMyFreelanceProfile() {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = freelanceProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("NO_FREELANCE_PROFILE",
                        "Vous n'avez pas encore créé de profil freelance", userId));
        return toDTO(fp);
    }

    public FreelanceProfileDTO createOrUpdateFreelanceProfile(FreelanceProfileRequest request) {
        UUID userId = profileService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        FreelanceProfile fp = freelanceProfileRepository.findByUserId(userId).orElse(null);
        boolean isNew = fp == null;

        if (isNew) {
            fp = new FreelanceProfile();
            fp.setUser(user);
        }

        fp.setTitle(request.getTitle());
        fp.setOverview(request.getOverview());
        fp.setCountry(request.getCountry() != null ? request.getCountry() : user.getCountry());
        fp.setCity(request.getCity() != null ? request.getCity() : user.getCity());
        fp.setHourlyRate(request.getHourlyRate());
        fp.setDailyRate(request.getDailyRate());
        fp.setMinProjectRate(request.getMinProjectRate());

        if (request.getCurrency() != null) {
            try { fp.setCurrency(Currency.valueOf(request.getCurrency())); } catch (IllegalArgumentException ignored) {}
        }
        if (request.getAvailabilityStatus() != null) {
            try { fp.setAvailabilityStatus(AvailabilityStatus.valueOf(request.getAvailabilityStatus())); } catch (IllegalArgumentException ignored) {}
        }
        if (request.getAvailabilityType() != null) {
            try { fp.setAvailabilityType(AvailabilityType.valueOf(request.getAvailabilityType())); } catch (IllegalArgumentException ignored) {}
        }
        fp.setWeeklyHours(request.getWeeklyHours());
        if (request.getProfileActive() != null) fp.setProfileActive(request.getProfileActive());

        fp = freelanceProfileRepository.save(fp);

        if (isNew) {
            profileService.recalculateCompletion(fp.getId());
        }

        return toDTO(fp);
    }

    public void toggleFreelanceProfile(boolean active) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = freelanceProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("NO_FREELANCE_PROFILE",
                        "Vous n'avez pas encore créé de profil freelance", userId));
        fp.setProfileActive(active);
        freelanceProfileRepository.save(fp);
    }

    // ===== SKILLS =====

    public List<SkillDTO> addSkill(SkillRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        if (fp.getSkills().size() >= 20) {
            throw new BusinessException("SKILL_LIMIT", "Vous ne pouvez pas ajouter plus de 20 compétences", null);
        }

        Skill skill = Skill.builder()
                .freelanceProfile(fp)
                .name(request.getName())
                .level(request.getLevel() != null ? SkillLevel.valueOf(request.getLevel()) : SkillLevel.INTERMEDIATE)
                .build();
        fp.getSkills().add(skill);
        freelanceProfileRepository.save(fp);
        profileService.recalculateCompletion(fp.getId());

        return fp.getSkills().stream().map(this::toSkillDTO).collect(Collectors.toList());
    }

    public List<SkillDTO> removeSkill(UUID skillId) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);
        fp.getSkills().removeIf(s -> s.getId().equals(skillId));
        freelanceProfileRepository.save(fp);
        profileService.recalculateCompletion(fp.getId());
        return fp.getSkills().stream().map(this::toSkillDTO).collect(Collectors.toList());
    }

    // ===== EXPERIENCES =====

    public List<WorkExperienceDTO> addExperience(WorkExperienceRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        WorkExperience exp = WorkExperience.builder()
                .freelanceProfile(fp)
                .jobTitle(request.getJobTitle())
                .company(request.getCompany())
                .country(request.getCountry())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .current(request.getCurrent() != null ? request.getCurrent() : false)
                .description(request.getDescription())
                .build();
        fp.getExperiences().add(exp);
        freelanceProfileRepository.save(fp);
        profileService.recalculateCompletion(fp.getId());

        return fp.getExperiences().stream().map(this::toExperienceDTO).collect(Collectors.toList());
    }

    public List<WorkExperienceDTO> updateExperience(UUID expId, WorkExperienceRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        WorkExperience exp = fp.getExperiences().stream()
                .filter(e -> e.getId().equals(expId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("EXPERIENCE_NOT_FOUND", "Expérience non trouvée", expId));

        exp.setJobTitle(request.getJobTitle());
        exp.setCompany(request.getCompany());
        exp.setCountry(request.getCountry());
        exp.setStartDate(request.getStartDate());
        exp.setEndDate(request.getEndDate());
        exp.setCurrent(request.getCurrent() != null ? request.getCurrent() : false);
        exp.setDescription(request.getDescription());

        freelanceProfileRepository.save(fp);
        return fp.getExperiences().stream().map(this::toExperienceDTO).collect(Collectors.toList());
    }

    public List<WorkExperienceDTO> removeExperience(UUID expId) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);
        fp.getExperiences().removeIf(e -> e.getId().equals(expId));
        freelanceProfileRepository.save(fp);
        profileService.recalculateCompletion(fp.getId());
        return fp.getExperiences().stream().map(this::toExperienceDTO).collect(Collectors.toList());
    }

    // ===== EDUCATIONS =====

    public List<EducationDTO> addEducation(EducationRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        Education edu = Education.builder()
                .freelanceProfile(fp)
                .institution(request.getInstitution())
                .degree(request.getDegree())
                .field(request.getField())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .build();
        fp.getEducations().add(edu);
        freelanceProfileRepository.save(fp);
        return fp.getEducations().stream().map(this::toEducationDTO).collect(Collectors.toList());
    }

    public List<EducationDTO> updateEducation(UUID eduId, EducationRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        Education edu = fp.getEducations().stream()
                .filter(e -> e.getId().equals(eduId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("EDUCATION_NOT_FOUND", "Formation non trouvée", eduId));

        edu.setInstitution(request.getInstitution());
        edu.setDegree(request.getDegree());
        edu.setField(request.getField());
        edu.setStartDate(request.getStartDate());
        edu.setEndDate(request.getEndDate());
        edu.setDescription(request.getDescription());

        freelanceProfileRepository.save(fp);
        return fp.getEducations().stream().map(this::toEducationDTO).collect(Collectors.toList());
    }

    public List<EducationDTO> removeEducation(UUID eduId) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);
        fp.getEducations().removeIf(e -> e.getId().equals(eduId));
        freelanceProfileRepository.save(fp);
        return fp.getEducations().stream().map(this::toEducationDTO).collect(Collectors.toList());
    }

    // ===== CERTIFICATIONS =====

    public List<CertificationDTO> addCertification(CertificationRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        Certification cert = Certification.builder()
                .freelanceProfile(fp)
                .name(request.getName())
                .issuer(request.getIssuer())
                .issueDate(request.getIssueDate())
                .verificationUrl(request.getVerificationUrl())
                .credentialId(request.getCredentialId())
                .build();
        fp.getCertifications().add(cert);
        freelanceProfileRepository.save(fp);
        profileService.recalculateCompletion(fp.getId());
        return fp.getCertifications().stream().map(this::toCertificationDTO).collect(Collectors.toList());
    }

    public List<CertificationDTO> removeCertification(UUID certId) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);
        fp.getCertifications().removeIf(c -> c.getId().equals(certId));
        freelanceProfileRepository.save(fp);
        profileService.recalculateCompletion(fp.getId());
        return fp.getCertifications().stream().map(this::toCertificationDTO).collect(Collectors.toList());
    }

    // ===== LANGUAGES =====

    public List<LanguageDTO> addLanguage(LanguageRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        Language lang = Language.builder()
                .freelanceProfile(fp)
                .languageName(request.getLanguageName())
                .level(request.getLevel() != null ? LanguageLevel.valueOf(request.getLevel()) : LanguageLevel.INTERMEDIAIRE)
                .build();
        fp.getLanguages().add(lang);
        freelanceProfileRepository.save(fp);
        return fp.getLanguages().stream().map(this::toLanguageDTO).collect(Collectors.toList());
    }

    public List<LanguageDTO> removeLanguage(UUID langId) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);
        fp.getLanguages().removeIf(l -> l.getId().equals(langId));
        freelanceProfileRepository.save(fp);
        return fp.getLanguages().stream().map(this::toLanguageDTO).collect(Collectors.toList());
    }

    // ===== PORTFOLIO =====

    public List<PortfolioItemDTO> addPortfolioItem(PortfolioItemRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        PortfolioItem item = PortfolioItem.builder()
                .freelanceProfile(fp)
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .projectUrl(request.getProjectUrl())
                .technologies(request.getTechnologies())
                .projectDate(request.getProjectDate())
                .role(request.getRole())
                .build();
        fp.getPortfolioItems().add(item);
        freelanceProfileRepository.save(fp);
        profileService.recalculateCompletion(fp.getId());
        return fp.getPortfolioItems().stream().map(this::toPortfolioDTO).collect(Collectors.toList());
    }

    public List<PortfolioItemDTO> updatePortfolioItem(UUID itemId, PortfolioItemRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        PortfolioItem item = fp.getPortfolioItems().stream()
                .filter(p -> p.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("PORTFOLIO_NOT_FOUND", "Réalisation non trouvée", itemId));

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setImageUrl(request.getImageUrl());
        item.setProjectUrl(request.getProjectUrl());
        item.setTechnologies(request.getTechnologies());
        item.setProjectDate(request.getProjectDate());
        item.setRole(request.getRole());

        freelanceProfileRepository.save(fp);
        return fp.getPortfolioItems().stream().map(this::toPortfolioDTO).collect(Collectors.toList());
    }

    public List<PortfolioItemDTO> removePortfolioItem(UUID itemId) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);
        fp.getPortfolioItems().removeIf(p -> p.getId().equals(itemId));
        freelanceProfileRepository.save(fp);
        profileService.recalculateCompletion(fp.getId());
        return fp.getPortfolioItems().stream().map(this::toPortfolioDTO).collect(Collectors.toList());
    }

    // ===== PROFESSIONAL LINKS =====

    public List<ProfessionalLinkDTO> addProfessionalLink(ProfessionalLinkRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        ProfessionalLink link = ProfessionalLink.builder()
                .freelanceProfile(fp)
                .platform(PlatformType.valueOf(request.getPlatform()))
                .url(request.getUrl())
                .build();
        fp.getProfessionalLinks().add(link);
        freelanceProfileRepository.save(fp);
        return fp.getProfessionalLinks().stream().map(this::toLinkDTO).collect(Collectors.toList());
    }

    public List<ProfessionalLinkDTO> removeProfessionalLink(UUID linkId) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);
        fp.getProfessionalLinks().removeIf(l -> l.getId().equals(linkId));
        freelanceProfileRepository.save(fp);
        return fp.getProfessionalLinks().stream().map(this::toLinkDTO).collect(Collectors.toList());
    }

    // ===== WORK PREFERENCES =====

    public WorkPreferenceDTO updateWorkPreference(WorkPreferenceRequest request) {
        UUID userId = profileService.getCurrentUserId();
        FreelanceProfile fp = getProfileByUserId(userId);

        WorkPreference pref = workPreferenceRepository.findByFreelanceProfileId(fp.getId()).orElse(null);
        if (pref == null) {
            pref = new WorkPreference();
            pref.setFreelanceProfile(fp);
        }
        pref.setWorkTypes(request.getWorkTypes());
        pref.setProjectTypes(request.getProjectTypes());
        pref.setAcceptedCountries(request.getAcceptedCountries());
        pref.setTimezone(request.getTimezone());
        workPreferenceRepository.save(pref);

        return WorkPreferenceDTO.builder()
                .id(pref.getId())
                .workTypes(pref.getWorkTypes())
                .projectTypes(pref.getProjectTypes())
                .acceptedCountries(pref.getAcceptedCountries())
                .timezone(pref.getTimezone())
                .build();
    }

    // ===== MAPPING HELPERS =====

    private FreelanceProfile getProfileByUserId(UUID userId) {
        return freelanceProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("NO_FREELANCE_PROFILE",
                        "Vous n'avez pas encore créé de profil freelance", userId));
    }

    private FreelanceProfileDTO toDTO(FreelanceProfile fp) {
        return FreelanceProfileDTO.builder()
                .id(fp.getId())
                .userId(fp.getUser().getId())
                .title(fp.getTitle())
                .overview(fp.getOverview())
                .country(fp.getCountry())
                .city(fp.getCity())
                .hourlyRate(fp.getHourlyRate())
                .dailyRate(fp.getDailyRate())
                .minProjectRate(fp.getMinProjectRate())
                .currency(fp.getCurrency() != null ? fp.getCurrency().name() : "XOF")
                .availabilityStatus(fp.getAvailabilityStatus() != null ? fp.getAvailabilityStatus().name() : null)
                .availabilityType(fp.getAvailabilityType() != null ? fp.getAvailabilityType().name() : null)
                .weeklyHours(fp.getWeeklyHours())
                .profileActive(fp.getProfileActive())
                .completionPercentage(fp.getCompletionPercentage())
                .isVerified(fp.getIsVerified())
                .skills(fp.getSkills().stream().map(this::toSkillDTO).collect(Collectors.toList()))
                .experiences(fp.getExperiences().stream().map(this::toExperienceDTO).collect(Collectors.toList()))
                .educations(fp.getEducations().stream().map(this::toEducationDTO).collect(Collectors.toList()))
                .certifications(fp.getCertifications().stream().map(this::toCertificationDTO).collect(Collectors.toList()))
                .languages(fp.getLanguages().stream().map(this::toLanguageDTO).collect(Collectors.toList()))
                .portfolioItems(fp.getPortfolioItems().stream().map(this::toPortfolioDTO).collect(Collectors.toList()))
                .professionalLinks(fp.getProfessionalLinks().stream().map(this::toLinkDTO).collect(Collectors.toList()))
                .workPreference(fp.getWorkPreference() != null ? WorkPreferenceDTO.builder()
                        .id(fp.getWorkPreference().getId())
                        .workTypes(fp.getWorkPreference().getWorkTypes())
                        .projectTypes(fp.getWorkPreference().getProjectTypes())
                        .acceptedCountries(fp.getWorkPreference().getAcceptedCountries())
                        .timezone(fp.getWorkPreference().getTimezone())
                        .build() : null)
                .build();
    }

    private SkillDTO toSkillDTO(Skill s) {
        return SkillDTO.builder().id(s.getId()).name(s.getName()).level(s.getLevel().name()).build();
    }

    private WorkExperienceDTO toExperienceDTO(WorkExperience e) {
        return WorkExperienceDTO.builder()
                .id(e.getId()).jobTitle(e.getJobTitle()).company(e.getCompany())
                .country(e.getCountry()).startDate(e.getStartDate()).endDate(e.getEndDate())
                .current(e.getCurrent()).description(e.getDescription()).build();
    }

    private EducationDTO toEducationDTO(Education e) {
        return EducationDTO.builder()
                .id(e.getId()).institution(e.getInstitution()).degree(e.getDegree())
                .field(e.getField()).startDate(e.getStartDate()).endDate(e.getEndDate())
                .description(e.getDescription()).build();
    }

    private CertificationDTO toCertificationDTO(Certification c) {
        return CertificationDTO.builder()
                .id(c.getId()).name(c.getName()).issuer(c.getIssuer())
                .issueDate(c.getIssueDate()).verificationUrl(c.getVerificationUrl())
                .credentialId(c.getCredentialId()).build();
    }

    private LanguageDTO toLanguageDTO(Language l) {
        return LanguageDTO.builder().id(l.getId()).languageName(l.getLanguageName()).level(l.getLevel().name()).build();
    }

    private PortfolioItemDTO toPortfolioDTO(PortfolioItem p) {
        return PortfolioItemDTO.builder()
                .id(p.getId()).title(p.getTitle()).description(p.getDescription())
                .imageUrl(p.getImageUrl()).projectUrl(p.getProjectUrl())
                .technologies(p.getTechnologies()).projectDate(p.getProjectDate()).role(p.getRole()).build();
    }

    private ProfessionalLinkDTO toLinkDTO(ProfessionalLink l) {
        return ProfessionalLinkDTO.builder().id(l.getId()).platform(l.getPlatform().name()).url(l.getUrl()).build();
    }
}
