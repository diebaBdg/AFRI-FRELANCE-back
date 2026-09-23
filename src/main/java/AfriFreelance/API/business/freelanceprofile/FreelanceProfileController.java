package AfriFreelance.API.business.freelanceprofile;

import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.business.badge.dtos.*;
import AfriFreelance.API.business.certification.dtos.*;
import AfriFreelance.API.business.clientprofile.dtos.*;
import AfriFreelance.API.business.company.dtos.*;
import AfriFreelance.API.business.education.dtos.*;
import AfriFreelance.API.business.favorite.dtos.*;
import AfriFreelance.API.business.language.dtos.*;
import AfriFreelance.API.business.portfolio.dtos.*;
import AfriFreelance.API.business.professionallink.dtos.*;
import AfriFreelance.API.business.skill.dtos.*;
import AfriFreelance.API.business.verification.dtos.*;
import AfriFreelance.API.business.workexperience.dtos.*;
import AfriFreelance.API.business.workpreference.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/freelance")
@RequiredArgsConstructor
@Tag(name = "Profil Freelance", description = "Gestion du profil freelance et de ses sections")
public class FreelanceProfileController {

    private final FreelanceProfileService freelanceProfileService;

    @GetMapping
    @Operation(summary = "Obtenir mon profil freelance")
    public ResponseEntity<FreelanceProfileDTO> getMyFreelanceProfile() {
        return ResponseEntity.ok(freelanceProfileService.getMyFreelanceProfile());
    }

    @PostMapping
    @Operation(summary = "Créer ou mettre à jour mon profil freelance")
    public ResponseEntity<FreelanceProfileDTO> createOrUpdate(@Valid @RequestBody FreelanceProfileRequest request) {
        return ResponseEntity.ok(freelanceProfileService.createOrUpdateFreelanceProfile(request));
    }

    @PutMapping("/toggle")
    @Operation(summary = "Activer ou désactiver mon profil freelance")
    public ResponseEntity<Void> toggleProfile(@RequestBody Map<String, Boolean> body) {
        freelanceProfileService.toggleFreelanceProfile(body.getOrDefault("active", true));
        return ResponseEntity.noContent().build();
    }

    // ===== SKILLS =====

    @PostMapping("/skills")
    @Operation(summary = "Ajouter une compétence")
    public ResponseEntity<List<SkillDTO>> addSkill(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(freelanceProfileService.addSkill(request));
    }

    @DeleteMapping("/skills/{skillId}")
    @Operation(summary = "Supprimer une compétence")
    public ResponseEntity<List<SkillDTO>> removeSkill(@PathVariable UUID skillId) {
        return ResponseEntity.ok(freelanceProfileService.removeSkill(skillId));
    }

    // ===== EXPERIENCES =====

    @PostMapping("/experiences")
    @Operation(summary = "Ajouter une expérience professionnelle")
    public ResponseEntity<List<WorkExperienceDTO>> addExperience(@Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.ok(freelanceProfileService.addExperience(request));
    }

    @PutMapping("/experiences/{expId}")
    @Operation(summary = "Modifier une expérience professionnelle")
    public ResponseEntity<List<WorkExperienceDTO>> updateExperience(@PathVariable UUID expId,
                                                                     @Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.ok(freelanceProfileService.updateExperience(expId, request));
    }

    @DeleteMapping("/experiences/{expId}")
    @Operation(summary = "Supprimer une expérience professionnelle")
    public ResponseEntity<List<WorkExperienceDTO>> removeExperience(@PathVariable UUID expId) {
        return ResponseEntity.ok(freelanceProfileService.removeExperience(expId));
    }

    // ===== EDUCATIONS =====

    @PostMapping("/educations")
    @Operation(summary = "Ajouter une formation")
    public ResponseEntity<List<EducationDTO>> addEducation(@Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(freelanceProfileService.addEducation(request));
    }

    @PutMapping("/educations/{eduId}")
    @Operation(summary = "Modifier une formation")
    public ResponseEntity<List<EducationDTO>> updateEducation(@PathVariable UUID eduId,
                                                               @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(freelanceProfileService.updateEducation(eduId, request));
    }

    @DeleteMapping("/educations/{eduId}")
    @Operation(summary = "Supprimer une formation")
    public ResponseEntity<List<EducationDTO>> removeEducation(@PathVariable UUID eduId) {
        return ResponseEntity.ok(freelanceProfileService.removeEducation(eduId));
    }

    // ===== CERTIFICATIONS =====

    @PostMapping("/certifications")
    @Operation(summary = "Ajouter une certification")
    public ResponseEntity<List<CertificationDTO>> addCertification(@Valid @RequestBody CertificationRequest request) {
        return ResponseEntity.ok(freelanceProfileService.addCertification(request));
    }

    @DeleteMapping("/certifications/{certId}")
    @Operation(summary = "Supprimer une certification")
    public ResponseEntity<List<CertificationDTO>> removeCertification(@PathVariable UUID certId) {
        return ResponseEntity.ok(freelanceProfileService.removeCertification(certId));
    }

    // ===== LANGUAGES =====

    @PostMapping("/languages")
    @Operation(summary = "Ajouter une langue")
    public ResponseEntity<List<LanguageDTO>> addLanguage(@Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.ok(freelanceProfileService.addLanguage(request));
    }

    @DeleteMapping("/languages/{langId}")
    @Operation(summary = "Supprimer une langue")
    public ResponseEntity<List<LanguageDTO>> removeLanguage(@PathVariable UUID langId) {
        return ResponseEntity.ok(freelanceProfileService.removeLanguage(langId));
    }

    // ===== PORTFOLIO =====

    @PostMapping("/portfolio")
    @Operation(summary = "Ajouter une réalisation au portfolio")
    public ResponseEntity<List<PortfolioItemDTO>> addPortfolioItem(@Valid @RequestBody PortfolioItemRequest request) {
        return ResponseEntity.ok(freelanceProfileService.addPortfolioItem(request));
    }

    @PutMapping("/portfolio/{itemId}")
    @Operation(summary = "Modifier une réalisation du portfolio")
    public ResponseEntity<List<PortfolioItemDTO>> updatePortfolioItem(@PathVariable UUID itemId,
                                                                       @Valid @RequestBody PortfolioItemRequest request) {
        return ResponseEntity.ok(freelanceProfileService.updatePortfolioItem(itemId, request));
    }

    @DeleteMapping("/portfolio/{itemId}")
    @Operation(summary = "Supprimer une réalisation du portfolio")
    public ResponseEntity<List<PortfolioItemDTO>> removePortfolioItem(@PathVariable UUID itemId) {
        return ResponseEntity.ok(freelanceProfileService.removePortfolioItem(itemId));
    }

    // ===== PROFESSIONAL LINKS =====

    @PostMapping("/links")
    @Operation(summary = "Ajouter un lien professionnel")
    public ResponseEntity<List<ProfessionalLinkDTO>> addLink(@Valid @RequestBody ProfessionalLinkRequest request) {
        return ResponseEntity.ok(freelanceProfileService.addProfessionalLink(request));
    }

    @DeleteMapping("/links/{linkId}")
    @Operation(summary = "Supprimer un lien professionnel")
    public ResponseEntity<List<ProfessionalLinkDTO>> removeLink(@PathVariable UUID linkId) {
        return ResponseEntity.ok(freelanceProfileService.removeProfessionalLink(linkId));
    }

    // ===== WORK PREFERENCES =====

    @PutMapping("/preferences")
    @Operation(summary = "Mettre à jour mes préférences de travail")
    public ResponseEntity<WorkPreferenceDTO> updatePreferences(@Valid @RequestBody WorkPreferenceRequest request) {
        return ResponseEntity.ok(freelanceProfileService.updateWorkPreference(request));
    }
}
