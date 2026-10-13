package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.enums.Currency;

import java.util.Map;

public interface PaymentProviderService {

    String getProviderName();

    ProviderInitResponse initiateCheckout(String transactionRef, double amount, Currency currency,
                                          String customerPhone, String successUrl, String errorUrl);

    boolean verifyPayment(String providerSessionId);

    boolean refundPayment(String providerTransactionId, double amount, Currency currency);

    WebhookResult parseWebhook(Map<String, Object> payload, Map<String, String> headers);
}
