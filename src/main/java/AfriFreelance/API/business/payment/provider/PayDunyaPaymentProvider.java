package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.enums.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayDunyaPaymentProvider implements PaymentProviderService {

    private static final String SANDBOX_BASE = "https://sandbox-api.paydunya.com";
    private static final String PROD_BASE = "https://app.paydunya.com/api/v1";

    @Value("${paydunya.master-key:}")
    private String masterKey;

    @Value("${paydunya.private-key:}")
    private String privateKey;

    @Value("${paydunya.token:}")
    private String token;

    @Value("${paydunya.sandbox:true}")
    private boolean sandbox;

    @Value("${paydunya.store-name:AfriFreelance}")
    private String storeName;

    @Value("${paydunya.store-website-url:https://afrifreelance.com}")
    private String storeWebsiteUrl;

    @Value("${paydunya.store-logo-url:}")
    private String storeLogoUrl;

    @Value("${paydunya.store-notification-url:}")
    private String storeNotificationUrl;

    @Value("${paydunya.store-cancel-url:}")
    private String storeCancelUrl;

    @Value("${paydunya.store-return-url:}")
    private String storeReturnUrl;

    private final RestClient restClient;

    public PayDunyaPaymentProvider() {
        this.restClient = RestClient.create();
    }

    private String getBaseUrl() {
        return sandbox ? SANDBOX_BASE + "/v1" : PROD_BASE;
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("PAYDUNYA-MASTER-KEY", masterKey);
        headers.set("PAYDUNYA-PRIVATE-KEY", privateKey);
        headers.set("PAYDUNYA-TOKEN", token);
        return headers;
    }

    @Override
    public String getProviderName() {
        return "PAYDUNYA";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProviderInitResponse initiateCheckout(String transactionRef, double amount, Currency currency,
                                                  String customerPhone, String successUrl, String errorUrl) {
        try {
            String url = getBaseUrl() + "/checkout-invoice/create";

            Map<String, Object> invoice = new HashMap<>();
            invoice.put("total_amount", (long) amount);
            invoice.put("description", "Paiement AfriFreelance - " + transactionRef);

            if (customerPhone != null && !customerPhone.isBlank()) {
                Map<String, Object> customer = new HashMap<>();
                customer.put("phone", customerPhone);
                invoice.put("customer", customer);
            }

            Map<String, Object> store = new HashMap<>();
            store.put("name", storeName);
            if (storeWebsiteUrl != null && !storeWebsiteUrl.isBlank()) {
                store.put("website_url", storeWebsiteUrl);
            }
            if (storeLogoUrl != null && !storeLogoUrl.isBlank()) {
                store.put("logo_url", storeLogoUrl);
            }

            String cancelUrl = errorUrl != null ? errorUrl : null;
            String returnUrl = successUrl != null ? successUrl : null;
            if (storeCancelUrl != null && !storeCancelUrl.isBlank()) {
                cancelUrl = storeCancelUrl;
            }
            if (storeReturnUrl != null && !storeReturnUrl.isBlank()) {
                returnUrl = storeReturnUrl;
            }
            if (cancelUrl != null) store.put("cancel_url", cancelUrl);
            if (returnUrl != null) store.put("return_url", returnUrl);

            String notificationUrl = storeNotificationUrl;
            if (notificationUrl == null || notificationUrl.isBlank()) {
                notificationUrl = returnUrl;
            }
            if (notificationUrl != null && !notificationUrl.isBlank()) {
                store.put("callback_url", notificationUrl);
            }

            Map<String, Object> customData = new HashMap<>();
            customData.put("transaction_ref", transactionRef);

            Map<String, Object> body = new HashMap<>();
            body.put("invoice", invoice);
            body.put("store", store);
            body.put("custom_data", customData);

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .headers(h -> h.addAll(buildAuthHeaders()))
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                log.error("PayDunya returned null response for txRef={}", transactionRef);
                throw new RuntimeException("Réponse vide de PayDunya");
            }

            String responseCode = (String) response.get("response_code");
            String responseText = (String) response.get("response_text");
            String payToken = (String) response.get("token");

            if (!"00".equals(responseCode) || payToken == null) {
                String desc = (String) response.get("description");
                log.error("PayDunya checkout failed: code={}, text={}, desc={}, txRef={}",
                        responseCode, responseText, desc, transactionRef);
                throw new RuntimeException("PayDunya: " + (desc != null ? desc : responseText));
            }

            String checkoutUrl = (sandbox ? "https://sandbox-app.paydunya.com" : "https://app.paydunya.com")
                    + "/checkout/" + payToken;

            log.info("PayDunya checkout created: txRef={}, token={}", transactionRef, payToken);

            return ProviderInitResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .providerSessionId(payToken)
                    .build();

        } catch (Exception e) {
            log.error("PayDunya checkout initiation failed for txRef={}: {}", transactionRef, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'initiation du paiement PayDunya: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean verifyPayment(String providerSessionId) {
        try {
            String url = getBaseUrl() + "/checkout-invoice/confirm/" + providerSessionId;

            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(buildAuthHeaders()))
                    .retrieve()
                    .body(Map.class);

            if (response == null) return false;

            String responseCode = (String) response.get("response_code");
            String responseText = (String) response.get("response_text");

            return "00".equals(responseCode)
                    && (responseText != null && responseText.toLowerCase().contains("success"));
        } catch (Exception e) {
            log.error("PayDunya payment verification failed for token={}: {}", providerSessionId, e.getMessage());
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean refundPayment(String providerTransactionId, double amount, Currency currency) {
        try {
            String url = getBaseUrl() + "/checkout-invoice/refund/" + providerTransactionId;

            Map<String, Object> body = new HashMap<>();
            body.put("amount", (long) amount);

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .headers(h -> h.addAll(buildAuthHeaders()))
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return false;
            String responseCode = (String) response.get("response_code");
            boolean success = "00".equals(responseCode);

            if (success) {
                log.info("PayDunya refund successful for token={}", providerTransactionId);
            } else {
                log.warn("PayDunya refund returned code={} for token={}", responseCode, providerTransactionId);
            }
            return success;
        } catch (Exception e) {
            log.error("PayDunya refund failed for token={}: {}", providerTransactionId, e.getMessage());
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public WebhookResult parseWebhook(Map<String, Object> payload, Map<String, String> headers) {
        if (payload == null || payload.isEmpty()) {
            return WebhookResult.builder().success(false).build();
        }

        String responseCode = (String) payload.get("response_code");
        if (!"00".equals(responseCode)) {
            log.info("PayDunya webhook ignored: response_code={}", responseCode);
            return WebhookResult.builder().success(false).build();
        }

        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) {
            data = payload;
        }

        Map<String, Object> invoice = (Map<String, Object>) data.get("invoice");
        String payToken = invoice != null ? (String) invoice.get("token") : null;
        if (payToken == null) {
            payToken = (String) data.get("token");
        }

        Map<String, Object> customData = (Map<String, Object>) data.get("custom_data");
        String txRef = null;
        if (customData != null) {
            txRef = (String) customData.get("transaction_ref");
        }
        if (txRef == null && invoice != null) {
            txRef = (String) invoice.get("token");
        }

        String status = (String) data.get("status");
        String receiptNo = (String) data.get("receipt_no");
        if (receiptNo == null && invoice != null) {
            receiptNo = (String) invoice.get("receipt_no");
        }

        Object amountObj = null;
        if (invoice != null) {
            amountObj = invoice.get("total_amount");
        }
        if (amountObj == null) {
            amountObj = data.get("total_amount");
        }
        double amount = 0;
        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).doubleValue();
        }

        boolean success = "completed".equalsIgnoreCase(status)
                || "success".equalsIgnoreCase(status)
                || "00".equals(responseCode);

        return WebhookResult.builder()
                .transactionRef(txRef)
                .providerTransactionId(receiptNo != null ? receiptNo : payToken)
                .success(success)
                .amount(amount)
                .build();
    }
}
