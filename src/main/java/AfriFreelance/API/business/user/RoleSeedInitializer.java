package AfriFreelance.API.business.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RoleSeedInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seed("ADMIN", "Administrateur", "Accès complet à toutes les fonctionnalités", 5);
        seed("INSTRUCTEUR", "Instructeur", "Vérifie et approuve les demandes", 3);
        seed("DEMANDEUR", "Demandeur", "Rôle par défaut à l'inscription", 1);
        seed("SUPERVISEUR", "Superviseur", "Assigne les demandes aux instructeurs", 4);
    }

    private void seed(String code, String libelle, String description, int niveau) {
        if (roleRepository.findByCode(code).isPresent()) {
            return;
        }
        roleRepository.save(Role.builder()
                .code(code)
                .libelle(libelle)
                .description(description)
                .niveauAutorisation(niveau)
                .actif(true)
                .build());
        log.info("Rôle {} créé", code);
    }
}
