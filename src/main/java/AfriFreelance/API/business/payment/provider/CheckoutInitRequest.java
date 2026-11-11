package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.enums.Currency;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CheckoutInitRequest {
    String transactionRef;
    double amount;
    Currency currency;
    String customerPhone;
    String customerName;
    String customerEmail;
    String itemName;
    String description;
    String successUrl;
    String errorUrl;
    String channel;
}
