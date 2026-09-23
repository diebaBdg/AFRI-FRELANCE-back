package AfriFreelance.API.business.verification;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile/verifications")
@RequiredArgsConstructor
@Tag(name = "Vérifications & Badges", description = "Vérification du compte et badges")
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping
    @Operation(summary = "Obtenir mes vérifications et progression")
    public ResponseEntity<VerificationSummaryDTO> getMyVerifications() {
        return ResponseEntity.ok(verificationService.getMyVerifications());
    }

    @PostMapping("/{verificationType}")
    @Operation(summary = "Demander une vérification (POC: simule le début du processus)")
    public ResponseEntity<VerificationDTO> requestVerification(@PathVariable String verificationType) {
        return ResponseEntity.ok(verificationService.requestVerification(verificationType.toUpperCase()));
    }

    @PostMapping("/{verificationType}/simulate")
    @Operation(summary = "Simuler la validation d'une vérification (POC uniquement)")
    public ResponseEntity<VerificationDTO> simulateVerification(@PathVariable String verificationType) {
        return ResponseEntity.ok(verificationService.simulateVerification(verificationType.toUpperCase()));
    }

    @GetMapping("/badges")
    @Operation(summary = "Obtenir mes badges")
    public ResponseEntity<List<BadgeDTO>> getMyBadges() {
        return ResponseEntity.ok(verificationService.getMyBadges());
    }
}
