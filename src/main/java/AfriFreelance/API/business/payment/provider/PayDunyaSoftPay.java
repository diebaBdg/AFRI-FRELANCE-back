package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.config.exceptions.BusinessException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PayDunya SoftPay channel ids and request bodies.
 * @see <a href="https://developers.paydunya.com/doc/FR/softpay">PayDunya SoftPay</a>
 */
public final class PayDunyaSoftPay {

    private PayDunyaSoftPay() {}

    public record Method(String id, String label, String country, boolean softPay, String softPayPath) {}

    public static final List<Method> METHODS = List.of(
            new Method("wave-senegal", "Wave Sénégal", "SN", true, "/softpay/wave-senegal"),
            new Method("orange-money-senegal", "Orange Money Sénégal", "SN", true, "/softpay/new-orange-money-senegal"),
            new Method("djamo", "Djamo SN", "SN", true, "/softpay/djamo"),
            new Method("card", "Carte bancaire", "INT", false, null),
            new Method("free-money-senegal", "Free Money Sénégal", "SN", true, "/softpay/free-money-senegal"),
            new Method("expresso-senegal", "Expresso SN", "SN", true, "/softpay/expresso-senegal")
    );

    public static String normalizeChannel(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String key = raw.trim().toLowerCase(Locale.ROOT)
                .replace(' ', '-')
                .replace('_', '-');
        return switch (key) {
            case "wave", "wave-sn", "wave-senegal" -> "wave-senegal";
            case "orange", "orange-money", "orange-money-sn", "orange-money-senegal", "om", "om-sn" -> "orange-money-senegal";
            case "djamo", "djamo-sn", "djamo-senegal" -> "djamo";
            case "card", "carte", "carte-bancaire" -> "card";
            case "free", "free-money", "free-money-senegal" -> "free-money-senegal";
            case "expresso", "expresso-sn", "expresso-senegal" -> "expresso-senegal";
            default -> key;
        };
    }

    public static Method requireMethod(String raw) {
        String id = normalizeChannel(raw);
        if (id == null) {
            throw new BusinessException("CHANNEL_REQUIRED", "Le moyen de paiement est obligatoire (wave-senegal, orange-money-senegal, djamo, card)", null);
        }
        return METHODS.stream()
                .filter(m -> m.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "UNKNOWN_CHANNEL",
                        "Moyen de paiement non supporté: " + raw,
                        raw
                ));
    }

    public static Map<String, Object> requestBody(String channel, String name, String email, String phone, String invoiceToken) {
        Method method = requireMethod(channel);
        if (!method.softPay()) {
            throw new BusinessException(
                    "SOFTPAY_NOT_AVAILABLE",
                    "La carte bancaire se paie via la page de checkout PayDunya (checkoutUrl).",
                    method.id()
            );
        }
        String safeName = blankTo(name, "Client AfriFreelance");
        String safeEmail = blankTo(email, "paiement@afrifreelance.com");
        String safePhone = digits(phone);
        if (safePhone.length() < 8) {
            throw new BusinessException("PHONE_REQUIRED", "Un numéro de téléphone Mobile Money est obligatoire", phone);
        }
        if (invoiceToken == null || invoiceToken.isBlank()) {
            throw new BusinessException("INVOICE_TOKEN_MISSING", "Token de facture PayDunya manquant", null);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        switch (method.id()) {
            case "wave-senegal" -> {
                body.put("wave_senegal_fullName", safeName);
                body.put("wave_senegal_email", safeEmail);
                body.put("wave_senegal_phone", safePhone);
                body.put("wave_senegal_payment_token", invoiceToken);
            }
            case "orange-money-senegal" -> {
                body.put("customer_name", safeName);
                body.put("customer_email", safeEmail);
                body.put("phone_number", safePhone);
                body.put("invoice_token", invoiceToken);
            }
            case "djamo" -> {
                body.put("djamo_fullName", safeName);
                body.put("djamo_email", safeEmail);
                body.put("djamo_phone", safePhone);
                body.put("djamo_payment_token", invoiceToken);
            }
            case "free-money-senegal" -> {
                body.put("customer_name", safeName);
                body.put("customer_email", safeEmail);
                body.put("phone_number", safePhone);
                body.put("payment_token", invoiceToken);
            }
            case "expresso-senegal" -> {
                body.put("expresso_sn_fullName", safeName);
                body.put("expresso_sn_email", safeEmail);
                body.put("expresso_sn_phone", safePhone);
                body.put("payment_token", invoiceToken);
            }
            default -> throw new BusinessException("UNKNOWN_CHANNEL", "Canal SoftPay inconnu: " + method.id(), method.id());
        }
        return body;
    }

    public static SoftPayResult parseResponse(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            return SoftPayResult.builder().success(false).message("Réponse vide de PayDunya SoftPay").build();
        }
        boolean success = Boolean.TRUE.equals(response.get("success"))
                || "00".equals(String.valueOf(response.get("response_code")));
        String message = firstNonBlank(
                stringValue(response.get("message")),
                stringValue(response.get("response_text")),
                stringValue(response.get("description"))
        );
        String url = stringValue(response.get("url"));
        Map<String, Object> other = asMap(response.get("other_url"));
        return SoftPayResult.builder()
                .success(success)
                .message(message)
                .url(url)
                .omUrl(stringValue(other.get("om_url")))
                .maxitUrl(stringValue(other.get("maxit_url")))
                .build();
    }

    public static List<Method> enabled(String channelsCsv) {
        if (channelsCsv == null || channelsCsv.isBlank()) {
            return METHODS.stream().filter(m -> List.of("wave-senegal", "orange-money-senegal", "card").contains(m.id())).toList();
        }
        List<String> wanted = java.util.Arrays.stream(channelsCsv.split(","))
                .map(PayDunyaSoftPay::normalizeChannel)
                .filter(s -> s != null && !s.isBlank())
                .toList();
        Map<String, Method> byId = new HashMap<>();
        for (Method method : METHODS) {
            byId.put(method.id(), method);
        }
        return wanted.stream().map(byId::get).filter(java.util.Objects::nonNull).toList();
    }

    static String digits(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("\\D", "");
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
