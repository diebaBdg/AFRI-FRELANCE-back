package AfriFreelance.API.business.profile;

import AfriFreelance.API.business.profile.dtos.*;
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
