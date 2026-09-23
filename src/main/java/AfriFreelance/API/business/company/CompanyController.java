package AfriFreelance.API.business.company;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile/company")
@RequiredArgsConstructor
@Tag(name = "Entreprise", description = "Gestion de l'entreprise et des membres")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "Obtenir mon entreprise")
    public ResponseEntity<CompanyDTO> getMyCompany() {
        return ResponseEntity.ok(companyService.getMyCompany());
    }

    @PostMapping
    @Operation(summary = "Créer mon entreprise")
    public ResponseEntity<CompanyDTO> createCompany(@Valid @RequestBody CompanyRequest request) {
        return new ResponseEntity<>(companyService.createCompany(request), HttpStatus.CREATED);
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Mettre à jour mon entreprise")
    public ResponseEntity<CompanyDTO> updateCompany(@PathVariable UUID companyId,
                                                     @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(companyId, request));
    }

    @DeleteMapping("/{companyId}")
    @Operation(summary = "Supprimer mon entreprise")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }

    // ===== MEMBERS =====

    @GetMapping("/{companyId}/members")
    @Operation(summary = "Obtenir les membres de l'entreprise")
    public ResponseEntity<List<CompanyMemberDTO>> getMembers(@PathVariable UUID companyId) {
        return ResponseEntity.ok(companyService.getMembers(companyId));
    }

    @PostMapping("/{companyId}/members")
    @Operation(summary = "Ajouter un membre à l'entreprise")
    public ResponseEntity<List<CompanyMemberDTO>> addMember(@PathVariable UUID companyId,
                                                             @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.ok(companyService.addMember(companyId, request));
    }

    @PutMapping("/{companyId}/members/{memberId}")
    @Operation(summary = "Modifier le rôle d'un membre")
    public ResponseEntity<List<CompanyMemberDTO>> updateMemberRole(@PathVariable UUID companyId,
                                                                    @PathVariable UUID memberId,
                                                                    @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(companyService.updateMemberRole(companyId, memberId, body.get("role")));
    }

    @DeleteMapping("/{companyId}/members/{memberId}")
    @Operation(summary = "Retirer un membre de l'entreprise")
    public ResponseEntity<Void> removeMember(@PathVariable UUID companyId, @PathVariable UUID memberId) {
        companyService.removeMember(companyId, memberId);
        return ResponseEntity.noContent().build();
    }
}
