-- =====================================================================
-- AfriFreelance — Payment provider integration fields
-- =====================================================================

ALTER TABLE td_payments
    ADD COLUMN IF NOT EXISTS checkout_url TEXT,
    ADD COLUMN IF NOT EXISTS provider_session_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(20),
    ADD COLUMN IF NOT EXISTS webhook_verified BOOLEAN DEFAULT FALSE;
