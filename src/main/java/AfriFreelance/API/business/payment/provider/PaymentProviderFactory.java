package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.config.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentProviderFactory {

    private final List<PaymentProviderService> providers;

    private Map<String, PaymentProviderService> providerMap;

    private Map<String, PaymentProviderService> getProviderMap() {
        if (providerMap == null) {
            providerMap = providers.stream()
                    .collect(Collectors.toMap(
                            p -> p.getProviderName().toUpperCase(),
                            p -> p
                    ));
        }
        return providerMap;
    }

    public PaymentProviderService getProvider(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("PROVIDER_REQUIRED", "Le fournisseur de paiement est obligatoire", null);
        }
        PaymentProviderService provider = getProviderMap().get(name.toUpperCase());
        if (provider == null) {
            throw new BusinessException("UNKNOWN_PROVIDER",
                    "Fournisseur de paiement non supporté: " + name + ". Fournisseurs disponibles: " + getProviderMap().keySet(),
                    name);
        }
        return provider;
    }
}
