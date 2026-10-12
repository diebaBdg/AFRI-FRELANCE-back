package AfriFreelance.API.business.proposal;

import AfriFreelance.API.business.proposal.dtos.*;
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
@Tag(name = "Candidatures & Propositions", description = "Soumission et gestion des candidatures")
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping("/projects/{projectId}/proposals")
    @Operation(summary = "Soumettre une candidature sur une mission")
    public ResponseEntity<ProposalDTO> createProposal(@PathVariable UUID projectId,
                                                        @Valid @RequestBody ProposalRequest request) {
        return new ResponseEntity<>(proposalService.createProposal(projectId, request), HttpStatus.CREATED);
    }

    @GetMapping("/projects/{projectId}/proposals")
    @Operation(summary = "Obtenir les candidatures d'une mission (client uniquement)")
    public ResponseEntity<List<ProposalDTO>> getProjectProposals(@PathVariable UUID projectId) {
        return ResponseEntity.ok(proposalService.getProposalsForProject(projectId));
    }

    @GetMapping("/proposals/me")
    @Operation(summary = "Obtenir mes candidatures")
    public ResponseEntity<List<ProposalDTO>> getMyProposals() {
        return ResponseEntity.ok(proposalService.getMyProposals());
    }

    @GetMapping("/proposals/{proposalId}")
    @Operation(summary = "Obtenir le détail d'une candidature")
    public ResponseEntity<ProposalDTO> getProposal(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(proposalService.getProposal(proposalId));
    }

    @PatchMapping("/proposals/{proposalId}/status")
    @Operation(summary = "Accepter, refuser ou retirer une candidature")
    public ResponseEntity<ProposalDTO> updateProposalStatus(@PathVariable UUID proposalId,
                                                              @RequestBody ProposalStatusRequest request) {
        return ResponseEntity.ok(proposalService.updateProposalStatus(proposalId, request.getStatus()));
    }

    @DeleteMapping("/proposals/{proposalId}")
    @Operation(summary = "Supprimer une candidature")
    public ResponseEntity<Void> deleteProposal(@PathVariable UUID proposalId) {
        proposalService.deleteProposal(proposalId);
        return ResponseEntity.noContent().build();
    }
}
