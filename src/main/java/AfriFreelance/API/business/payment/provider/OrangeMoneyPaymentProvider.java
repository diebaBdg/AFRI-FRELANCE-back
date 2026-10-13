package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.enums.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class OrangeMoneyPaymentProvider implements PaymentProviderService {

    private static final String API_BASE = "https://api.orange.com";
    private static final String OM_BASE = "https://api.orange.com/orange-payment-web/v3";

    @Value("${om.client-id:}")
    private String clientId;

    @Value("${om.client-secret:}")
    private String clientSecret;

    @Value("${om.merchant-key:}")
    private String merchantKey;

    @Value("${om.sandbox:true}")
    private boolean sandbox;

    private final RestClient restClient;

    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt;

    public OrangeMoneyPaymentProvider() {
        this.restClient = RestClient.create();
    }

    @Override
    public String getProviderName() {
        return "ORANGE_MONEY";
    }

    private String getOauthToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(30))) {
            return cachedToken;
        }

        try {
            String credentials = clientId + ":" + clientSecret;
            String authHeader = "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "client_credentials");

            Map<String, Object> response = restClient.post()
                    .uri(API_BASE + "/oauth/v3/token")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("access_token") == null) {
                throw new RuntimeException("Token OAuth Orange Money non obtenu");
            }

            cachedToken = "Bearer " + response.get("access_token");
            int expiresIn = response.get("expires_in") instanceof Number
                    ? ((Number) response.get("expires_in")).intValue()
                    : 3600;
            tokenExpiresAt = Instant.now().plusSeconds(expiresIn);

            log.info("Orange Money OAuth token refreshed, expires in {}s", expiresIn);
            return cachedToken;

        } catch (Exception e) {
            log.error("Failed to get Orange Money OAuth token: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur d'authentification Orange Money", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProviderInitResponse initiateCheckout(String transactionRef, double amount, Currency currency,
                                                  String customerPhone, String successUrl, String errorUrl) {
        try {
            String url = OM_BASE + "/webpayment";

            Map<String, Object> body = Map.of(
                    "merchant_key", merchantKey,
                    "currency", currency.name().equals("XOF") ? "XOF" : currency.name(),
                    "order_id", transactionRef,
                    "amount", (long) amount,
                    "return_url", successUrl,
                    "cancel_url", errorUrl,
                    "notif_url", successUrl,
                    "lang", "fr"
            );

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, getOauthToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new RuntimeException("Réponse vide d'Orange Money");
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) {
                data = response;
            }

            String checkoutUrl = (String) data.get("payment_url");
            String sessionId = (String) data.get("pay_token");
            if (sessionId == null) {
                sessionId = (String) data.get("id");
            }

            log.info("Orange Money checkout session created: txRef={}, sessionId={}", transactionRef, sessionId);

            return ProviderInitResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .providerSessionId(sessionId)
                    .build();

        } catch (Exception e) {
            log.error("Orange Money checkout initiation failed for txRef={}: {}", transactionRef, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'initiation du paiement Orange Money: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean verifyPayment(String providerSessionId) {
        try {
            String url = OM_BASE + "/webpayment/" + providerSessionId;

            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, getOauthToken())
                    .retrieve()
                    .body(Map.class);

            if (response == null) return false;

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) {
                data = response;
            }

            String status = (String) data.get("status");
            return "SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status);
        } catch (Exception e) {
            log.error("Orange Money payment verification failed for session={}: {}", providerSessionId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean refundPayment(String providerTransactionId, double amount, Currency currency) {
        try {
            String url = OM_BASE + "/webpayment/" + providerTransactionId + "/refund";

            Map<String, Object> body = Map.of(
                    "merchant_key", merchantKey,
                    "amount", (long) amount,
                    "currency", currency.name()
            );

            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, getOauthToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Orange Money refund successful for session={}", providerTransactionId);
            return true;
        } catch (Exception e) {
            log.error("Orange Money refund failed for session={}: {}", providerTransactionId, e.getMessage());
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public WebhookResult parseWebhook(Map<String, Object> payload, Map<String, String> headers) {
        if (payload == null || payload.isEmpty()) {
            return WebhookResult.builder().success(false).build();
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) {
            data = payload;
        }

        String txRef = (String) data.get("order_id");
        if (txRef == null) {
            txRef = (String) data.get("tx_id");
        }

        String providerTxId = (String) data.get("pay_token");
        if (providerTxId == null) {
            providerTxId = (String) data.get("id");
        }

        String status = (String) data.get("status");
        boolean success = "SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status);

        Object amountObj = data.get("amount");
        double amount = 0;
        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).doubleValue();
        }

        return WebhookResult.builder()
                .transactionRef(txRef)
                .providerTransactionId(providerTxId)
                .success(success)
                .amount(amount)
                .build();
    }
}
