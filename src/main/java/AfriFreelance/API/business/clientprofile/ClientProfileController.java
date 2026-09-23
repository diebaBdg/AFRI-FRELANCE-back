package AfriFreelance.API.business.clientprofile;

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

import java.util.Map;

@RestController
@RequestMapping("/api/profile/client")
@RequiredArgsConstructor
@Tag(name = "Profil Client", description = "Gestion du profil client")
public class ClientProfileController {

    private final ClientProfileService clientProfileService;

    @GetMapping
    @Operation(summary = "Obtenir mon profil client")
    public ResponseEntity<ClientProfileDTO> getMyClientProfile() {
        return ResponseEntity.ok(clientProfileService.getMyClientProfile());
    }

    @PostMapping
    @Operation(summary = "Créer ou mettre à jour mon profil client")
    public ResponseEntity<ClientProfileDTO> createOrUpdate(@Valid @RequestBody ClientProfileRequest request) {
        return ResponseEntity.ok(clientProfileService.createOrUpdateClientProfile(request));
    }

    @PutMapping("/toggle")
    @Operation(summary = "Activer ou désactiver mon profil client")
    public ResponseEntity<Void> toggleProfile(@RequestBody Map<String, Boolean> body) {
        clientProfileService.toggleClientProfile(body.getOrDefault("active", true));
        return ResponseEntity.noContent().build();
    }
}
