package AfriFreelance.API.business.security;

import AfriFreelance.API.auth.dtos.ChangePasswordRequest;
import AfriFreelance.API.auth.dtos.VerifyPasswordRequest;
import AfriFreelance.API.auth.dtos.VerifyPasswordResponse;
import AfriFreelance.API.business.security.dto.SecurityInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/security")
@RequiredArgsConstructor
@Tag(name = "Sécurité", description = "Gestion du mot de passe et de la sécurité du compte")
public class SecurityController {

    private final SecurityService securityService;

    @GetMapping
    @Operation(summary = "Obtenir les informations de sécurité du compte")
    public ResponseEntity<SecurityInfoDTO> getSecurityInfo() {
        return ResponseEntity.ok(securityService.getSecurityInfo());
    }

    @PutMapping("/password")
    @Operation(summary = "Changer le mot de passe")
    public ResponseEntity<SecurityInfoDTO> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(securityService.changePassword(request));
    }

    @PostMapping("/verify-password")
    @Operation(summary = "Vérifier le mot de passe actuel (pour confirmation d'action sensible)")
    public ResponseEntity<VerifyPasswordResponse> verifyPassword(@RequestBody VerifyPasswordRequest request) {
        return ResponseEntity.ok(securityService.verifyPassword(request));
    }
}
