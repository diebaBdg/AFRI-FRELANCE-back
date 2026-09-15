package AfriFreelance.API.business.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Order(11)
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private static final Set<String> SUPERVISEUR_PERMISSIONS = Set.of(
            "DEMANDE_READ", "DEMANDE_ASSIGN", "DEMANDE_REASSIGN", "DEMANDE_APPROVE", "DEMANDE_STATS",
            "DOCUMENT_DOWNLOAD", "NOTIFICATION_READ", "PAIEMENT_READ", "TYPE_AGREMENT_READ", "USER_READ"
    );

    @Override
    @Transactional
    public void run(String... args) {
        seedSuperviseurRole();
    }

    private void seedSuperviseurRole() {
        Role superviseur = roleRepository.findByCode("SUPERVISEUR").orElseGet(() -> {
            Role role = Role.builder()
                    .code("SUPERVISEUR")
                    .libelle("Superviseur")
                    .description("Assigne les demandes aux instructeurs et approuve apres paiement")
                    .niveauAutorisation(4)
                    .actif(true)
                    .dateCreation(LocalDateTime.now())
                    .build();
            log.info("Role SUPERVISEUR cree");
            return roleRepository.save(role);
        });

        for (String code : SUPERVISEUR_PERMISSIONS) {
            permissionRepository.findByCode(code).ifPresent(permission -> {
                if (!rolePermissionRepository.existsByRoleIdAndPermissionId(superviseur.getId(), permission.getId())) {
                    rolePermissionRepository.save(RolePermission.builder()
                            .role(superviseur)
                            .permission(permission)
                            .dateAssignation(LocalDateTime.now())
                            .build());
                }
            });
        }

        removeWorkflowPermissionsFromRole("ADMIN", List.of("DEMANDE_ASSIGN", "DEMANDE_REASSIGN", "DEMANDE_APPROVE"));
        removeWorkflowPermissionsFromRole("INSTRUCTEUR", List.of("DEMANDE_ASSIGN", "DEMANDE_REASSIGN", "DEMANDE_APPROVE"));
    }

    private void removeWorkflowPermissionsFromRole(String roleCode, List<String> permissionCodes) {
        roleRepository.findByCode(roleCode).ifPresent(role -> {
            for (String code : permissionCodes) {
                permissionRepository.findByCode(code).ifPresent(permission ->
                        rolePermissionRepository.deleteByRoleIdAndPermissionId(role.getId(), permission.getId()));
            }
        });
    }
}
