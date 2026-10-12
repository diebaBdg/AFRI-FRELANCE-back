package AfriFreelance.API.business.contract;

import AfriFreelance.API.business.contract.dtos.*;
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
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "Contrats & Livraisons", description = "Gestion des contrats et des livrables")
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/me")
    @Operation(summary = "Obtenir mes contrats (client ou freelance)")
    public ResponseEntity<List<ContractDTO>> getMyContracts() {
        return ResponseEntity.ok(contractService.getMyContracts());
    }

    @GetMapping("/{contractId}")
    @Operation(summary = "Obtenir le détail d'un contrat")
    public ResponseEntity<ContractDTO> getContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(contractService.getContract(contractId));
    }

    @PatchMapping("/{contractId}/status")
    @Operation(summary = "Changer le statut d'un contrat")
    public ResponseEntity<ContractDTO> updateContractStatus(@PathVariable UUID contractId,
                                                              @RequestBody ContractStatusRequest request) {
        return ResponseEntity.ok(contractService.updateContractStatus(contractId, request.getStatus()));
    }

    @PostMapping("/{contractId}/deliveries")
    @Operation(summary = "Soumettre un livrable (freelance)")
    public ResponseEntity<DeliveryDTO> submitDelivery(@PathVariable UUID contractId,
                                                        @Valid @RequestBody DeliveryRequest request) {
        return new ResponseEntity<>(contractService.submitDelivery(contractId, request), HttpStatus.CREATED);
    }

    @PostMapping("/{contractId}/approve-delivery")
    @Operation(summary = "Valider ou refuser un livrable (client)")
    public ResponseEntity<DeliveryDTO> approveDelivery(@PathVariable UUID contractId,
                                                         @RequestBody DeliveryReviewRequest request) {
        return ResponseEntity.ok(contractService.approveDelivery(contractId, request));
    }
}
