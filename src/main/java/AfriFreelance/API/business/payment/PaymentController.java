package AfriFreelance.API.business.payment;

import AfriFreelance.API.business.payment.dtos.*;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Paiements & Commissions", description = "Gestion des paiements, séquestre et reversements")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/escrow/initiate")
    @Operation(summary = "Initier un dépôt en séquestre (escrow) pour un contrat")
    public ResponseEntity<PaymentDTO> initiateEscrow(@Valid @RequestBody PaymentInitRequest request) {
        return new ResponseEntity<>(paymentService.initiateEscrow(request), HttpStatus.CREATED);
    }

    @PostMapping("/{paymentId}/confirm")
    @Operation(summary = "Confirmer un paiement (après validation fournisseur)")
    public ResponseEntity<PaymentDTO> confirmPayment(@PathVariable UUID paymentId,
                                                      @RequestBody PaymentActionRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentId, request));
    }

    @PostMapping("/{paymentId}/release")
    @Operation(summary = "Libérer le paiement au freelance")
    public ResponseEntity<PaymentDTO> releasePayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.releasePayment(paymentId));
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Rembourser le client (en cas de litige)")
    public ResponseEntity<PaymentDTO> refundPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtenir mon historique de paiements")
    public ResponseEntity<List<PaymentDTO>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Obtenir les paiements d'un contrat")
    public ResponseEntity<List<PaymentDTO>> getContractPayments(@PathVariable UUID contractId) {
        return ResponseEntity.ok(paymentService.getContractPayments(contractId));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Obtenir le détail d'un paiement")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
