package AfriFreelance.API.business.notification;

import AfriFreelance.API.business.notification.dto.NotificationPreferenceDTO;
import AfriFreelance.API.business.notification.dto.NotificationPreferenceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/notifications")
@RequiredArgsConstructor
@Tag(name = "Préférences de notifications", description = "Gestion des préférences de notification")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping
    @Operation(summary = "Obtenir mes préférences de notification")
    public ResponseEntity<NotificationPreferenceDTO> getPreferences() {
        return ResponseEntity.ok(preferenceService.getMyPreferences());
    }

    @PutMapping
    @Operation(summary = "Mettre à jour mes préférences de notification")
    public ResponseEntity<NotificationPreferenceDTO> updatePreferences(@RequestBody NotificationPreferenceRequest request) {
        return ResponseEntity.ok(preferenceService.updatePreferences(request));
    }
}
