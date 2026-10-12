package AfriFreelance.API.business.moderation;

import AfriFreelance.API.business.moderation.dtos.*;
import AfriFreelance.API.enums.ReportStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Modération", description = "Signalements, litiges et suspension de comptes")
public class ModerationController {

    private final ModerationService moderationService;

    @PostMapping("/reports")
    @Operation(summary = "Signaler un utilisateur, une mission, une candidature ou un avis")
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody ReportRequest request) {
        return new ResponseEntity<>(moderationService.createReport(request), HttpStatus.CREATED);
    }

    @GetMapping("/reports/me")
    @Operation(summary = "Obtenir mes signalements")
    public ResponseEntity<List<ReportDTO>> getMyReports() {
        return ResponseEntity.ok(moderationService.getMyReports());
    }

    @GetMapping("/admin/reports")
    @Operation(summary = "Obtenir tous les signalements (admin)")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        return ResponseEntity.ok(moderationService.getAllReports());
    }

    @GetMapping("/admin/reports/{status}")
    @Operation(summary = "Obtenir les signalements par statut (admin)")
    public ResponseEntity<List<ReportDTO>> getReportsByStatus(@PathVariable ReportStatus status) {
        return ResponseEntity.ok(moderationService.getReportsByStatus(status));
    }

    @PatchMapping("/admin/reports/{reportId}/resolve")
    @Operation(summary = "Traiter un signalement (admin)")
    public ResponseEntity<ReportDTO> resolveReport(@PathVariable UUID reportId,
                                                     @Valid @RequestBody ReportResolutionRequest request) {
        return ResponseEntity.ok(moderationService.resolveReport(reportId, request));
    }
}
