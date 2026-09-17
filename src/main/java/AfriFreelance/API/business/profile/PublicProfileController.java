package AfriFreelance.API.business.profile;

import AfriFreelance.API.business.profile.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Profils Publics", description = "Consultation des profils publics et recherche de freelancers")
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    @GetMapping("/freelancers/{username}")
    @Operation(summary = "Consulter le profil public d'un freelancer")
    public ResponseEntity<PublicFreelanceProfileDTO> getPublicFreelanceProfile(@PathVariable String username) {
        return ResponseEntity.ok(publicProfileService.getPublicFreelanceProfile(username));
    }

    @GetMapping("/clients/{username}")
    @Operation(summary = "Consulter le profil public d'un client")
    public ResponseEntity<PublicClientProfileDTO> getPublicClientProfile(@PathVariable String username) {
        return ResponseEntity.ok(publicProfileService.getPublicClientProfile(username));
    }

    @GetMapping("/freelancers")
    @Operation(summary = "Rechercher des freelancers")
    public ResponseEntity<List<FreelancerCardDTO>> searchFreelancers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(publicProfileService.searchFreelancers(query, country, limit));
    }
}
