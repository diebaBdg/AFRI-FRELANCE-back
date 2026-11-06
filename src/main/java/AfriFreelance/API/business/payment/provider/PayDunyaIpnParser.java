package AfriFreelance.API.business.payment.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PayDunya IPN arrives as {@code application/x-www-form-urlencoded} with a nested
 * {@code data} payload. JSON callbacks are also accepted for local tests.
 */
public final class PayDunyaIpnParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PayDunyaIpnParser() {}

    public static Map<String, Object> normalizePayload(Map<String, ?> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }

        Object dataNode = raw.get("data");
        if (dataNode instanceof Map<?, ?> dataMap) {
            return castMap(dataMap);
        }
        if (dataNode instanceof String dataJson && !dataJson.isBlank()) {
            Map<String, Object> parsed = readJsonObject(dataJson);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }

        Map<String, Object> unflattened = unflatten(raw);
        Object nested = unflattened.get("data");
        if (nested instanceof Map<?, ?> nestedMap) {
            return castMap(nestedMap);
        }
        if (nested instanceof String nestedJson) {
            Map<String, Object> parsed = readJsonObject(nestedJson);
            if (!parsed.isEmpty()) {
                return parsed;
            }
        }

        if (raw.containsKey("status") || raw.containsKey("invoice") || raw.containsKey("custom_data")) {
            return castMap(raw);
        }
        return unflattened;
    }

    public static boolean isCompleted(Map<String, Object> data) {
        String status = stringValue(data.get("status"));
        return "completed".equalsIgnoreCase(status);
    }

    public static boolean isValidHash(Map<String, Object> data, String masterKey) {
        if (masterKey == null || masterKey.isBlank()) {
            return false;
        }
        String received = stringValue(data.get("hash"));
        if (received == null || received.isBlank()) {
            return false;
        }
        String expected = sha512Hex(masterKey);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                received.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String transactionRef(Map<String, Object> data) {
        Map<String, Object> customData = asMap(data.get("custom_data"));
        String ref = stringValue(customData.get("transaction_ref"));
        if (ref == null) {
            ref = stringValue(customData.get("transactionRef"));
        }
        return ref;
    }

    public static String invoiceToken(Map<String, Object> data) {
        Map<String, Object> invoice = asMap(data.get("invoice"));
        String token = stringValue(invoice.get("token"));
        if (token == null) {
            token = stringValue(data.get("token"));
        }
        return token;
    }

    public static String receiptOrToken(Map<String, Object> data) {
        String receipt = stringValue(data.get("receipt_url"));
        if (receipt != null && !receipt.isBlank()) {
            return receipt;
        }
        return invoiceToken(data);
    }

    public static double totalAmount(Map<String, Object> data) {
        Map<String, Object> invoice = asMap(data.get("invoice"));
        Object amount = invoice.get("total_amount");
        if (amount == null) {
            amount = data.get("total_amount");
        }
        if (amount instanceof Number number) {
            return number.doubleValue();
        }
        if (amount != null) {
            try {
                return Double.parseDouble(amount.toString());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    public static String sha512Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                hex.append(String.format(Locale.ROOT, "%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-512 unavailable", e);
        }
    }

    static Map<String, Object> unflatten(Map<String, ?> raw) {
        Map<String, Object> root = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : raw.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            putPath(root, entry.getKey(), entry.getValue());
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    private static void putPath(Map<String, Object> root, String key, Object value) {
        List<String> parts = pathParts(key);
        if (parts.isEmpty()) {
            return;
        }
        Map<String, Object> current = root;
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (i == parts.size() - 1) {
                current.put(part, value);
                return;
            }
            Object next = current.get(part);
            if (!(next instanceof Map<?, ?>)) {
                Map<String, Object> child = new LinkedHashMap<>();
                current.put(part, child);
                current = child;
            } else {
                current = (Map<String, Object>) next;
            }
        }
    }

    private static List<String> pathParts(String key) {
        List<String> parts = new ArrayList<>();
        int i = 0;
        while (i < key.length()) {
            if (key.charAt(i) == '[') {
                int end = key.indexOf(']', i);
                if (end < 0) {
                    parts.add(key.substring(i));
                    break;
                }
                parts.add(key.substring(i + 1, end));
                i = end + 1;
            } else {
                int bracket = key.indexOf('[', i);
                if (bracket < 0) {
                    parts.add(key.substring(i));
                    break;
                }
                parts.add(key.substring(i, bracket));
                i = bracket;
            }
        }
        parts.removeIf(String::isBlank);
        return parts;
    }

    private static Map<String, Object> readJsonObject(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> source) {
        return (Map<String, Object>) source;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        if (value instanceof String json && json.trim().startsWith("{")) {
            return readJsonObject(json);
        }
        return Map.of();
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
