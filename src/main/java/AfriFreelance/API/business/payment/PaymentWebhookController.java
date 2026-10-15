package AfriFreelance.API.business.payment;

import AfriFreelance.API.business.payment.provider.PaymentProviderFactory;
import AfriFreelance.API.business.payment.provider.PaymentProviderService;
import AfriFreelance.API.business.payment.provider.WebhookResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhooks/payments")
@RequiredArgsConstructor
@Tag(name = "Webhooks Paiement", description = "Callbacks des fournisseurs de paiement (PayDunya)")
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final PaymentProviderFactory providerFactory;

    @PostMapping("/{provider}")
    @Operation(summary = "Webhook callback pour les notifications de paiement")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String provider,
            @RequestBody Map<String, Object> payload,
            @RequestHeader Map<String, String> headers) {

        log.info("Webhook reçu de {} avec payload keys: {}", provider,
                payload != null ? payload.keySet() : "null");

        try {
            PaymentProviderService providerService = providerFactory.getProvider(provider);
            WebhookResult result = providerService.parseWebhook(payload, headers);

            if (result == null || !result.isSuccess() || result.getTransactionRef() == null) {
                log.info("Webhook {} ignoré (non pertinent ou non réussi)", provider);
                return ResponseEntity.ok("OK");
            }

            paymentService.confirmPaymentFromWebhook(
                    result.getTransactionRef(),
                    result.getProviderTransactionId()
            );

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Erreur traitement webhook {}: {}", provider, e.getMessage(), e);
            return ResponseEntity.ok("OK");
        }
    }
}
