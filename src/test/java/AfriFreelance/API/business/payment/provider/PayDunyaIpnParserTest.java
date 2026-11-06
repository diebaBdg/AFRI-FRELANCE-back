package AfriFreelance.API.business.payment.provider;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayDunyaIpnParserTest {

    @Test
    void unflattensFormEncodedIpnAndValidatesHash() {
        String masterKey = "test-master-key";
        String hash = PayDunyaIpnParser.sha512Hex(masterKey);

        Map<String, Object> form = new LinkedHashMap<>();
        form.put("data[status]", "completed");
        form.put("data[hash]", hash);
        form.put("data[invoice][token]", "test_abc123");
        form.put("data[invoice][total_amount]", "15000");
        form.put("data[custom_data][transaction_ref]", "ESC-12345678-1");

        Map<String, Object> data = PayDunyaIpnParser.normalizePayload(form);

        assertTrue(PayDunyaIpnParser.isCompleted(data));
        assertTrue(PayDunyaIpnParser.isValidHash(data, masterKey));
        assertEquals("ESC-12345678-1", PayDunyaIpnParser.transactionRef(data));
        assertEquals("test_abc123", PayDunyaIpnParser.invoiceToken(data));
        assertEquals(15000.0, PayDunyaIpnParser.totalAmount(data));
    }

    @Test
    void rejectsInvalidHashAndNonCompletedStatus() {
        Map<String, Object> data = Map.of(
                "status", "cancelled",
                "hash", "not-a-real-hash",
                "invoice", Map.of("token", "test_x")
        );

        assertFalse(PayDunyaIpnParser.isCompleted(data));
        assertFalse(PayDunyaIpnParser.isValidHash(data, "master"));
    }
}
