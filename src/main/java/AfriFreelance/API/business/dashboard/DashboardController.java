package AfriFreelance.API.business.dashboard;

import AfriFreelance.API.business.dashboard.dto.DashboardDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Tableau de bord contextuel selon le mode actif")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Obtenir le dashboard selon le mode actif (Freelance ou Client)")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/freelance")
    @Operation(summary = "Obtenir le dashboard freelance")
    public ResponseEntity<DashboardDTO> getFreelanceDashboard() {
        return ResponseEntity.ok(dashboardService.getFreelanceDashboard());
    }

    @GetMapping("/client")
    @Operation(summary = "Obtenir le dashboard client")
    public ResponseEntity<DashboardDTO> getClientDashboard() {
        return ResponseEntity.ok(dashboardService.getClientDashboard());
    }
}
