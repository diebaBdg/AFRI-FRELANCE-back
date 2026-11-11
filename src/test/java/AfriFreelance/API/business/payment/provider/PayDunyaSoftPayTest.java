package AfriFreelance.API.business.payment.provider;

import AfriFreelance.API.config.exceptions.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PayDunyaSoftPayTest {

    @Test
    void buildsWaveSoftPayBody() {
        Map<String, Object> body = PayDunyaSoftPay.requestBody(
                "WAVE", "Awa Diop", "awa@test.sn", "+221 77 123 45 67", "test_token"
        );
        assertEquals("Awa Diop", body.get("wave_senegal_fullName"));
        assertEquals("awa@test.sn", body.get("wave_senegal_email"));
        assertEquals("221771234567", body.get("wave_senegal_phone"));
        assertEquals("test_token", body.get("wave_senegal_payment_token"));
    }

    @Test
    void buildsOrangeMoneyQrBody() {
        Map<String, Object> body = PayDunyaSoftPay.requestBody(
                "orange-money-senegal", "Awa", "awa@test.sn", "771234567", "inv_1"
        );
        assertEquals("Awa", body.get("customer_name"));
        assertEquals("771234567", body.get("phone_number"));
        assertEquals("inv_1", body.get("invoice_token"));
    }

    @Test
    void cardHasNoSoftPay() {
        assertThrows(BusinessException.class, () ->
                PayDunyaSoftPay.requestBody("card", "Awa", "a@b.c", "771234567", "tok"));
    }

    @Test
    void parsesOrangeMoneyRedirect() {
        SoftPayResult result = PayDunyaSoftPay.parseResponse(Map.of(
                "success", true,
                "message", "Rediriger",
                "url", "https://app.paydunya.com/recharge-orange-sn",
                "other_url", Map.of("om_url", "https://om.example", "maxit_url", "https://maxit.example")
        ));
        assertTrue(result.isSuccess());
        assertEquals("https://om.example", result.getOmUrl());
        assertEquals("https://maxit.example", result.getMaxitUrl());
    }

    @Test
    void enabledFiltersConfiguredChannels() {
        var methods = PayDunyaSoftPay.enabled("wave-senegal, card");
        assertEquals(2, methods.size());
        assertEquals("wave-senegal", methods.get(0).id());
        assertFalse(methods.get(1).softPay());
    }
}
