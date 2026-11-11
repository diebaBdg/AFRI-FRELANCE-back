package AfriFreelance.API.business.payment.provider;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SoftPayResult {
    boolean success;
    String message;
    String url;
    String omUrl;
    String maxitUrl;
}
