package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PayDunyaPaymentProvider implements PaymentProviderService {

    static final String SANDBOX_API = "https://app.paydunya.com/sandbox-api/v1";
    static final String PROD_API = "https://app.paydunya.com/api/v1";
    static final String SANDBOX_CHECKOUT = "https://app.paydunya.com/sandbox-checkout/invoice/";
    static final String PROD_CHECKOUT = "https://app.paydunya.com/checkout/invoice/";

    @Value("${paydunya.master-key:}")
    private String masterKey;

    @Value("${paydunya.private-key:}")
    private String privateKey;

    @Value("${paydunya.token:}")
    private String token;

    @Value("${paydunya.public-key:}")
    private String publicKey;

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

    @Value("${paydunya.channels:wave-senegal,orange-money-senegal}")
    private String channels;

    @Value("${app.backend.url:http://localhost:8082}")
    private String backendUrl;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    private final RestClient restClient;

    public PayDunyaPaymentProvider() {
        this.restClient = RestClient.create();
    }

    private String getBaseUrl() {
        return sandbox ? SANDBOX_API : PROD_API;
    }

    private void requireCredentials() {
        if (isBlank(masterKey) || isBlank(privateKey) || isBlank(token)) {
            throw new BusinessException(
                    "PAYDUNYA_NOT_CONFIGURED",
                    "Les clés PayDunya de test sont manquantes. Renseigne PAYDUNYA_MASTER_KEY, PAYDUNYA_PRIVATE_KEY et PAYDUNYA_TOKEN dans le fichier env.",
                    null
            );
        }
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("PAYDUNYA-MASTER-KEY", masterKey);
        headers.set("PAYDUNYA-PRIVATE-KEY", privateKey);
        headers.set("PAYDUNYA-TOKEN", token);
        if (!isBlank(publicKey)) {
            headers.set("PAYDUNYA-PUBLIC-KEY", publicKey);
        }
        return headers;
    }

    @Override
    public String getProviderName() {
        return "PAYDUNYA";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProviderInitResponse initiateCheckout(CheckoutInitRequest request) {
        requireCredentials();
        String transactionRef = request.getTransactionRef();
        try {
            String url = getBaseUrl() + "/checkout-invoice/create";

            Map<String, Object> invoice = new HashMap<>();
            invoice.put("total_amount", Math.round(request.getAmount()));
            invoice.put("description", firstNonBlank(
                    request.getDescription(),
                    "Paiement escrow AfriFreelance - " + transactionRef
            ));

            List<String> channelList = parseChannels(request.getChannel());
            if (!channelList.isEmpty()) {
                invoice.put("channels", channelList);
            }

            Map<String, Object> customer = new HashMap<>();
            if (!isBlank(request.getCustomerName())) {
                customer.put("name", request.getCustomerName());
            }
            if (!isBlank(request.getCustomerEmail())) {
                customer.put("email", request.getCustomerEmail());
            }
            if (!isBlank(request.getCustomerPhone())) {
                customer.put("phone", request.getCustomerPhone());
            }
            if (!customer.isEmpty()) {
                invoice.put("customer", customer);
            }

            if (!isBlank(request.getItemName())) {
                Map<String, Object> item0 = new HashMap<>();
                item0.put("name", request.getItemName());
                item0.put("quantity", 1);
                item0.put("unit_price", String.valueOf(Math.round(request.getAmount())));
                item0.put("total_price", String.valueOf(Math.round(request.getAmount())));
                item0.put("description", invoice.get("description"));
                invoice.put("items", Map.of("item_0", item0));
            }

            Map<String, Object> store = new HashMap<>();
            store.put("name", storeName);
            if (!isBlank(storeWebsiteUrl)) {
                store.put("website_url", storeWebsiteUrl);
            }
            if (!isBlank(storeLogoUrl)) {
                store.put("logo_url", storeLogoUrl);
            }

            Map<String, Object> actions = new HashMap<>();
            String cancelUrl = firstNonBlank(storeCancelUrl, request.getErrorUrl(), frontendWalletUrl("cancel"));
            String returnUrl = firstNonBlank(storeReturnUrl, request.getSuccessUrl(), defaultReturnUrl());
            String callbackUrl = firstNonBlank(storeNotificationUrl, defaultIpnUrl());
            if (!isBlank(cancelUrl)) {
                actions.put("cancel_url", cancelUrl);
            }
            if (!isBlank(returnUrl)) {
                actions.put("return_url", returnUrl);
            }
            if (!isBlank(callbackUrl)) {
                actions.put("callback_url", callbackUrl);
            }

            Map<String, Object> customData = new HashMap<>();
            customData.put("transaction_ref", transactionRef);

            Map<String, Object> body = new HashMap<>();
            body.put("invoice", invoice);
            body.put("store", store);
            body.put("custom_data", customData);
            if (!actions.isEmpty()) {
                body.put("actions", actions);
            }

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .headers(h -> h.addAll(buildAuthHeaders()))
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new BusinessException("PAYDUNYA_EMPTY_RESPONSE", "Réponse vide de PayDunya", transactionRef);
            }

            String responseCode = stringValue(response.get("response_code"));
            String responseText = stringValue(response.get("response_text"));
            String payToken = stringValue(response.get("token"));

            if (!"00".equals(responseCode) || isBlank(payToken)) {
                String desc = stringValue(response.get("description"));
                log.error("PayDunya checkout failed: code={}, text={}, desc={}, txRef={}",
                        responseCode, responseText, desc, transactionRef);
                throw new BusinessException(
                        "PAYDUNYA_CHECKOUT_FAILED",
                        "PayDunya: " + firstNonBlank(desc, responseText, "échec de création de facture"),
                        transactionRef
                );
            }

            String checkoutUrl = responseText != null && responseText.startsWith("http")
                    ? responseText
                    : (sandbox ? SANDBOX_CHECKOUT : PROD_CHECKOUT) + payToken;

            log.info("PayDunya checkout created: txRef={}, token={}", transactionRef, payToken);

            return ProviderInitResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .providerSessionId(payToken)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("PayDunya checkout initiation failed for txRef={}: {}", transactionRef, e.getMessage(), e);
            throw new BusinessException(
                    "PAYDUNYA_CHECKOUT_ERROR",
                    "Erreur lors de l'initiation du paiement PayDunya: " + e.getMessage(),
                    transactionRef
            );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean verifyPayment(String providerSessionId) {
        requireCredentials();
        if (isBlank(providerSessionId)) {
            return false;
        }
        try {
            String url = getBaseUrl() + "/checkout-invoice/confirm/" + providerSessionId;

            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(buildAuthHeaders()))
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return false;
            }
            if (response.get("hash") != null && !PayDunyaIpnParser.isValidHash(response, masterKey)) {
                log.warn("PayDunya confirm hash invalid for token={}", providerSessionId);
                return false;
            }
            return PayDunyaIpnParser.isCompleted(response);
        } catch (Exception e) {
            log.error("PayDunya payment verification failed for token={}: {}", providerSessionId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean refundPayment(String providerTransactionId, double amount, Currency currency) {
        log.warn("PayDunya n'expose pas d'API checkout de remboursement. Remboursement manuel requis pour {}", providerTransactionId);
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SoftPayResult initiateSoftPay(String invoiceToken, String channel, String customerName,
                                         String customerEmail, String phone) {
        requireCredentials();
        PayDunyaSoftPay.Method method = PayDunyaSoftPay.requireMethod(channel);
        if (!method.softPay()) {
            throw new BusinessException(
                    "SOFTPAY_NOT_AVAILABLE",
                    "Utilise checkoutUrl pour payer par carte bancaire.",
                    method.id()
            );
        }
        Map<String, Object> body = PayDunyaSoftPay.requestBody(method.id(), customerName, customerEmail, phone, invoiceToken);
        try {
            String url = getBaseUrl() + method.softPayPath();
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .headers(h -> h.addAll(buildAuthHeaders()))
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            SoftPayResult result = PayDunyaSoftPay.parseResponse(response);
            if (!result.isSuccess()) {
                throw new BusinessException(
                        "PAYDUNYA_SOFTPAY_FAILED",
                        firstNonBlank(result.getMessage(), "Échec SoftPay PayDunya"),
                        method.id()
                );
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("PayDunya SoftPay failed channel={} token={}: {}", method.id(), invoiceToken, e.getMessage(), e);
            throw new BusinessException(
                    "PAYDUNYA_SOFTPAY_ERROR",
                    "Erreur SoftPay PayDunya: " + e.getMessage(),
                    method.id()
            );
        }
    }

    @Override
    public WebhookResult parseWebhook(Map<String, Object> payload, Map<String, String> headers) {
        Map<String, Object> data = PayDunyaIpnParser.normalizePayload(payload);
        if (data.isEmpty()) {
            return WebhookResult.builder().success(false).build();
        }

        if (!PayDunyaIpnParser.isValidHash(data, masterKey)) {
            log.warn("IPN PayDunya rejeté: hash invalide");
            return WebhookResult.builder().success(false).build();
        }

        boolean completed = PayDunyaIpnParser.isCompleted(data);
        String txRef = PayDunyaIpnParser.transactionRef(data);
        String invoiceToken = PayDunyaIpnParser.invoiceToken(data);

        if (!completed) {
            log.info("IPN PayDunya ignoré: status={}", data.get("status"));
            return WebhookResult.builder()
                    .success(false)
                    .transactionRef(txRef)
                    .invoiceToken(invoiceToken)
                    .build();
        }

        return WebhookResult.builder()
                .transactionRef(txRef)
                .invoiceToken(invoiceToken)
                .providerTransactionId(PayDunyaIpnParser.receiptOrToken(data))
                .success(true)
                .amount(PayDunyaIpnParser.totalAmount(data))
                .build();
    }

    private String defaultIpnUrl() {
        return publicApiBase() + "/webhooks/payments/paydunya";
    }

    private String defaultReturnUrl() {
        return publicApiBase() + "/webhooks/payments/paydunya/return";
    }

    private String frontendWalletUrl(String status) {
        String front = frontendUrl == null ? "http://localhost:5173" : frontendUrl.replaceAll("/+$", "");
        return front + "/wallet?status=" + status;
    }

    private String publicApiBase() {
        String base = backendUrl == null ? "" : backendUrl.replaceAll("/+$", "");
        String ctx = contextPath == null || "/".equals(contextPath) ? "" : contextPath;
        if (!ctx.startsWith("/")) {
            ctx = "/" + ctx;
        }
        return base + ctx;
    }

    private List<String> parseChannels(String requested) {
        if (!isBlank(requested)) {
            return List.of(PayDunyaSoftPay.normalizeChannel(requested));
        }
        return PayDunyaSoftPay.enabled(channels).stream().map(PayDunyaSoftPay.Method::id).toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
