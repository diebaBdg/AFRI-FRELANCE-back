package AfriFreelance.API.business.profile;

import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.enums.ActiveMode;
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
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profil", description = "Gestion du compte et profil utilisateur")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/summary")
    @Operation(summary = "Obtenir le résumé du profil (modes, statuts, badges)")
    public ResponseEntity<ProfileSummaryDTO> getProfileSummary() {
        return ResponseEntity.ok(profileService.getMyProfileSummary());
    }

    @GetMapping("/personal-info")
    @Operation(summary = "Obtenir mes informations personnelles")
    public ResponseEntity<PersonalInfoDTO> getPersonalInfo() {
        return ResponseEntity.ok(profileService.getMyPersonalInfo());
    }

    @PutMapping("/personal-info")
    @Operation(summary = "Mettre à jour mes informations personnelles")
    public ResponseEntity<PersonalInfoDTO> updatePersonalInfo(@Valid @RequestBody PersonalInfoRequest request) {
        return ResponseEntity.ok(profileService.updateMyPersonalInfo(request));
    }

    @PutMapping("/avatar")
    @Operation(summary = "Mettre à jour ma photo de profil")
    public ResponseEntity<Void> updateAvatar(@RequestBody Map<String, String> body) {
        profileService.updateAvatar(body.get("avatarUrl"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/avatar")
    @Operation(summary = "Supprimer ma photo de profil")
    public ResponseEntity<Void> removeAvatar() {
        profileService.removeAvatar();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/active-mode")
    @Operation(summary = "Changer de mode actif (Freelance / Client)")
    public ResponseEntity<ProfileSummaryDTO> switchActiveMode(@RequestBody ActiveModeRequest request) {
        ActiveMode mode = ActiveMode.valueOf(request.getMode().toUpperCase());
        return ResponseEntity.ok(profileService.switchActiveMode(mode));
    }

    @GetMapping("/completion")
    @Operation(summary = "Obtenir le taux de complétude du profil")
    public ResponseEntity<CompletionDTO> getCompletion() {
        return ResponseEntity.ok(profileService.getMyCompletion());
    }

    @GetMapping("/favorites")
    @Operation(summary = "Obtenir mes favoris")
    public ResponseEntity<List<FavoriteDTO>> getFavorites() {
        return ResponseEntity.ok(profileService.getMyFavorites());
    }

    @PostMapping("/favorites/{targetUserId}")
    @Operation(summary = "Ajouter un utilisateur à mes favoris")
    public ResponseEntity<Void> addFavorite(@PathVariable UUID targetUserId) {
        profileService.addFavorite(targetUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favorites/{targetUserId}")
    @Operation(summary = "Retirer un utilisateur de mes favoris")
    public ResponseEntity<Void> removeFavorite(@PathVariable UUID targetUserId) {
        profileService.removeFavorite(targetUserId);
        return ResponseEntity.noContent().build();
    }
}
