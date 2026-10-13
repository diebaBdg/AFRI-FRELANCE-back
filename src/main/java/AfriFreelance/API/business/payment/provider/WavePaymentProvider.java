package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.enums.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class WavePaymentProvider implements PaymentProviderService {

    private static final String API_BASE = "https://api.wave.com/v1";
    private static final String SANDBOX_BASE = "https://sandbox.api.wave.com/v1";

    @Value("${wave.api-key:}")
    private String apiKey;

    @Value("${wave.secret:}")
    private String apiSecret;

    @Value("${wave.sandbox:true}")
    private boolean sandbox;

    @Value("${wave.business-name:AfriFreelance}")
    private String businessName;

    private final RestClient restClient;

    public WavePaymentProvider() {
        this.restClient = RestClient.create();
    }

    private String getBaseUrl() {
        return sandbox ? SANDBOX_BASE : API_BASE;
    }

    private String getAuthHeader() {
        String credentials = apiKey + ":" + apiSecret;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Override
    public String getProviderName() {
        return "WAVE";
    }

    @Override
    public ProviderInitResponse initiateCheckout(String transactionRef, double amount, Currency currency,
                                                  String customerPhone, String successUrl, String errorUrl) {
        try {
            String url = getBaseUrl() + "/checkout/sessions";

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("amount", String.valueOf((long) amount));
            formData.add("currency", currency.name());
            formData.add("error_url", errorUrl);
            formData.add("success_url", successUrl);
            formData.add("name", businessName + " - " + transactionRef);

            if (customerPhone != null && !customerPhone.isBlank()) {
                String formatted = customerPhone.startsWith("+") ? customerPhone.substring(1) : customerPhone;
                formData.add("mobile_money_sender", formatted);
            }

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                log.error("Wave checkout returned null response for txRef={}", transactionRef);
                throw new RuntimeException("Réponse vide de Wave");
            }

            String checkoutUrl = (String) response.get("wave_launch_url");
            String sessionId = (String) response.get("id");

            if (checkoutUrl == null && sessionId != null) {
                checkoutUrl = "https://pay.wave.com/m/cmn/" + sessionId + "/";
            }

            log.info("Wave checkout session created: txRef={}, sessionId={}", transactionRef, sessionId);

            return ProviderInitResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .providerSessionId(sessionId)
                    .build();

        } catch (Exception e) {
            log.error("Wave checkout initiation failed for txRef={}: {}", transactionRef, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'initiation du paiement Wave: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyPayment(String providerSessionId) {
        try {
            String url = getBaseUrl() + "/checkout/sessions/" + providerSessionId;

            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .retrieve()
                    .body(Map.class);

            if (response == null) return false;

            String status = (String) response.get("payment_status");
            return "paid".equalsIgnoreCase(status);
        } catch (Exception e) {
            log.error("Wave payment verification failed for session={}: {}", providerSessionId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean refundPayment(String providerTransactionId, double amount, Currency currency) {
        try {
            String url = getBaseUrl() + "/refund";

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("checkout_session_id", providerTransactionId);
            formData.add("amount", String.valueOf((long) amount));
            formData.add("currency", currency.name());

            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Wave refund successful for session={}", providerTransactionId);
            return true;
        } catch (Exception e) {
            log.error("Wave refund failed for session={}: {}", providerTransactionId, e.getMessage());
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public WebhookResult parseWebhook(Map<String, Object> payload, Map<String, String> headers) {
        if (payload == null || payload.isEmpty()) {
            return WebhookResult.builder().success(false).build();
        }

        String eventType = (String) payload.get("type");

        if (!"checkout.session.completed".equals(eventType)) {
            log.info("Wave webhook event type '{}' ignored", eventType);
            return WebhookResult.builder().success(false).build();
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) {
            return WebhookResult.builder().success(false).build();
        }

        String txRef = (String) data.get("client_reference");
        if (txRef == null) {
            txRef = (String) data.get("id");
        }

        String providerTxId = (String) data.get("id");
        String status = (String) data.get("payment_status");

        Object amountObj = data.get("amount");
        double amount = 0;
        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).doubleValue();
        }

        boolean success = "paid".equalsIgnoreCase(status);

        return WebhookResult.builder()
                .transactionRef(txRef)
                .providerTransactionId(providerTxId)
                .success(success)
                .amount(amount)
                .build();
    }
}
