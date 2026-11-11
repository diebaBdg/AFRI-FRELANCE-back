package AfriFreelance.API.business.payment;

import AfriFreelance.API.business.payment.provider.PaymentProviderFactory;
import AfriFreelance.API.business.payment.provider.PaymentProviderService;
import AfriFreelance.API.business.payment.provider.WebhookResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhooks/payments")
@RequiredArgsConstructor
@Tag(name = "Webhooks Paiement", description = "Callbacks des fournisseurs de paiement (PayDunya)")
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final PaymentProviderFactory providerFactory;
    private final ObjectMapper objectMapper;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @PostMapping(value = "/{provider}", consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.ALL_VALUE
    })
    @Operation(summary = "IPN PayDunya (JSON ou application/x-www-form-urlencoded)")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String provider,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {

        Map<String, Object> payload = readPayload(request);
        log.info("Webhook reçu de {} avec payload keys: {}", provider, payload.keySet());

        try {
            PaymentProviderService providerService = providerFactory.getProvider(provider);
            WebhookResult result = providerService.parseWebhook(payload, headers);

            if (result == null || !result.isSuccess()) {
                log.info("Webhook {} ignoré (non pertinent ou non réussi)", provider);
                return ResponseEntity.ok("OK");
            }

            paymentService.confirmPaymentFromWebhook(result);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Erreur traitement webhook {}: {}", provider, e.getMessage(), e);
            return ResponseEntity.ok("OK");
        }
    }

    @GetMapping("/{provider}/return")
    @Operation(summary = "Return URL PayDunya après checkout")
    public ResponseEntity<Void> handleReturn(
            @PathVariable String provider,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "invoice_token", required = false) String invoiceToken) {
        String invoice = (token != null && !token.isBlank()) ? token : invoiceToken;
        String status = "pending";
        String paymentId = "";
        try {
            if (invoice != null && !invoice.isBlank()) {
                var dto = paymentService.confirmFromInvoiceToken(invoice);
                paymentId = dto.getId() != null ? dto.getId().toString() : "";
                status = "ESCROWED".equals(dto.getStatus()) ? "paid" : dto.getStatus().toLowerCase();
            }
        } catch (Exception e) {
            log.warn("Return URL {} : {}", provider, e.getMessage());
            status = "error";
        }
        String location = frontendUrl.replaceAll("/+$", "")
                + "/wallet?status=" + status
                + (paymentId.isBlank() ? "" : "&paymentId=" + paymentId);
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", location).build();
    }

    @GetMapping("/{provider}/cancel")
    @Operation(summary = "Cancel URL PayDunya")
    public ResponseEntity<Void> handleCancel() {
        String location = frontendUrl.replaceAll("/+$", "") + "/wallet?status=cancel";
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", location).build();
    }
}

    private Map<String, Object> readPayload(HttpServletRequest request) {
        try {
            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (body == null || body.isBlank()) {
                return Map.of();
            }
            String trimmed = body.trim();
            if (trimmed.startsWith("{")) {
                return objectMapper.readValue(trimmed, new TypeReference<>() {});
            }
            return parseForm(trimmed);
        } catch (Exception e) {
            log.warn("Impossible de lire le payload IPN: {}", e.getMessage());
            return Map.of();
        }
    }

    private static Map<String, Object> parseForm(String body) {
        Map<String, Object> form = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            String value = eq < 0 ? "" : pair.substring(eq + 1);
            form.put(decode(key), decode(value));
        }
        return form;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", " "), StandardCharsets.UTF_8);
    }
}
