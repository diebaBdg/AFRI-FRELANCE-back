package AfriFreelance.API.business.payment.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderInitResponse {
    private String checkoutUrl;
    private String providerSessionId;
}
