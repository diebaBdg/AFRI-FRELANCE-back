package AfriFreelance.API.business.admin;

import AfriFreelance.API.business.admin.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Statistiques, gestion des utilisateurs, missions et transactions")
public class AdminController {

    private final AdminService adminService;

    // ===== STATISTICS =====

    @GetMapping("/stats")
    @Operation(summary = "Obtenir les statistiques globales de la plateforme")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getPlatformStats());
    }

    // ===== USER MANAGEMENT =====

    @GetMapping("/users")
    @Operation(summary = "Lister tous les utilisateurs avec filtres optionnels")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getAllUsers(search, role, status));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Obtenir le détail d'un utilisateur")
    public ResponseEntity<AdminUserDTO> getUserDetail(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getUserDetail(userId));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Changer le statut d'un utilisateur (activer, suspendre, désactiver)")
    public ResponseEntity<AdminUserDTO> updateUserStatus(@PathVariable UUID userId,
                                                          @RequestBody AdminUserStatusRequest request) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, request.getStatus()));
    }

    @PatchMapping("/users/{userId}/roles")
    @Operation(summary = "Modifier les rôles d'un utilisateur")
    public ResponseEntity<AdminUserDTO> updateUserRoles(@PathVariable UUID userId,
                                                         @RequestBody AdminUserRoleRequest request) {
        return ResponseEntity.ok(adminService.updateUserRoles(userId, request.getRoleCodes()));
    }

    // ===== PROJECT MANAGEMENT =====

    @GetMapping("/projects")
    @Operation(summary = "Lister toutes les missions avec filtres optionnels")
    public ResponseEntity<List<AdminProjectDTO>> getAllProjects(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(adminService.getAllProjects(status, category));
    }

    @PatchMapping("/projects/{projectId}/status")
    @Operation(summary = "Forcer le changement de statut d'une mission")
    public ResponseEntity<AdminProjectDTO> updateProjectStatus(@PathVariable UUID projectId,
                                                                @RequestBody AdminProjectStatusRequest request) {
        return ResponseEntity.ok(adminService.updateProjectStatus(projectId, request.getStatus()));
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary = "Supprimer une mission (admin)")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId) {
        adminService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    // ===== TRANSACTION MANAGEMENT =====

    @GetMapping("/transactions")
    @Operation(summary = "Lister toutes les transactions avec filtres optionnels")
    public ResponseEntity<List<AdminTransactionDTO>> getAllTransactions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(adminService.getAllTransactions(status, type));
    }
}
