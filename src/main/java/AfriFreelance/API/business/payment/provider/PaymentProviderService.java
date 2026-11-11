package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.enums.Currency;

import java.util.Map;

public interface PaymentProviderService {

    String getProviderName();

    ProviderInitResponse initiateCheckout(CheckoutInitRequest request);

    boolean verifyPayment(String providerSessionId);

    boolean refundPayment(String providerTransactionId, double amount, Currency currency);

    WebhookResult parseWebhook(Map<String, Object> payload, Map<String, String> headers);

    SoftPayResult initiateSoftPay(String invoiceToken, String channel, String customerName,
                                  String customerEmail, String phone);
}
