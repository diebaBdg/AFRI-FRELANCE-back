-- =====================================================================
-- AfriFreelance — Categories, Files, Conversations, Messages,
-- Notifications, Projects, Proposals, Contracts, Deliveries, Payments
-- =====================================================================

-- ---------------------------------------------------------------------
-- Categories
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(200),
    icon_name       VARCHAR(50),
    is_active       BOOLEAN      DEFAULT TRUE,
    display_order   INTEGER      DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_categories_active ON td_categories(is_active);

-- ---------------------------------------------------------------------
-- Files
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_files (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_name   VARCHAR(255) NOT NULL,
    stored_name     VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    size            BIGINT       NOT NULL,
    uploaded_by_id  UUID         NOT NULL REFERENCES td_users(id) ON DELETE CASCADE,
    access_type     VARCHAR(20)  DEFAULT 'PRIVATE',
    contract_id     UUID,
    project_id      UUID,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_files_uploader   ON td_files(uploaded_by_id);
CREATE INDEX IF NOT EXISTS idx_files_contract   ON td_files(contract_id);
CREATE INDEX IF NOT EXISTS idx_files_project    ON td_files(project_id);

-- ---------------------------------------------------------------------
-- Projects
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_projects (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id                 UUID NOT NULL REFERENCES td_users(id),
    title                     VARCHAR(200) NOT NULL,
    description               TEXT,
    project_type              VARCHAR(20)  DEFAULT 'FIXED_PRICE',
    budget_min                DOUBLE PRECISION,
    budget_max                DOUBLE PRECISION,
    currency                  VARCHAR(10)  DEFAULT 'XOF',
    estimated_duration_days   INTEGER,
    expected_start_date       DATE,
    category                  VARCHAR(100),
    skills_required           TEXT[],
    country                   VARCHAR(100),
    is_remote                 BOOLEAN      DEFAULT TRUE,
    status                    VARCHAR(20)  DEFAULT 'OPEN',
    proposal_count            INTEGER      DEFAULT 0,
    assigned_freelance_id     UUID,
    created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_projects_client   ON td_projects(client_id);
CREATE INDEX IF NOT EXISTS idx_projects_status   ON td_projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_category ON td_projects(category);

-- ---------------------------------------------------------------------
-- Proposals
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_proposals (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id                UUID NOT NULL REFERENCES td_projects(id) ON DELETE CASCADE,
    freelance_id              UUID NOT NULL REFERENCES td_users(id),
    proposed_amount           DOUBLE PRECISION NOT NULL,
    currency                  VARCHAR(10)  DEFAULT 'XOF',
    estimated_duration_days   INTEGER,
    cover_letter              TEXT,
    attachment_url            TEXT,
    status                    VARCHAR(20)  DEFAULT 'PENDING',
    created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, freelance_id)
);

CREATE INDEX IF NOT EXISTS idx_proposals_project   ON td_proposals(project_id);
CREATE INDEX IF NOT EXISTS idx_proposals_freelance ON td_proposals(freelance_id);

-- ---------------------------------------------------------------------
-- Contracts
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_contracts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id         UUID NOT NULL UNIQUE REFERENCES td_proposals(id) ON DELETE CASCADE,
    client_id           UUID NOT NULL REFERENCES td_users(id),
    freelance_id        UUID NOT NULL REFERENCES td_users(id),
    amount              DOUBLE PRECISION NOT NULL,
    currency            VARCHAR(10)  DEFAULT 'XOF',
    start_date          DATE,
    end_date            DATE,
    estimated_days      INTEGER,
    status              VARCHAR(20)  DEFAULT 'ACTIVE',
    is_paid             BOOLEAN      DEFAULT FALSE,
    commission_rate     DOUBLE PRECISION DEFAULT 10.0,
    commission_amount   DOUBLE PRECISION,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_contracts_client    ON td_contracts(client_id);
CREATE INDEX IF NOT EXISTS idx_contracts_freelance ON td_contracts(freelance_id);

-- ---------------------------------------------------------------------
-- Deliveries
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_deliveries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id     UUID NOT NULL REFERENCES td_contracts(id) ON DELETE CASCADE,
    description     TEXT,
    attachment_url  TEXT,
    status          VARCHAR(20)  DEFAULT 'SUBMITTED',
    client_feedback TEXT,
    submitted_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at     TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_deliveries_contract ON td_deliveries(contract_id);

-- ---------------------------------------------------------------------
-- Payments
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_payments (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_ref         VARCHAR(100) NOT NULL UNIQUE,
    contract_id             UUID NOT NULL REFERENCES td_contracts(id) ON DELETE CASCADE,
    payer_id                UUID NOT NULL REFERENCES td_users(id),
    payee_id                UUID REFERENCES td_users(id),
    payment_type            VARCHAR(20)  NOT NULL,
    amount                  DOUBLE PRECISION NOT NULL,
    currency                VARCHAR(10)  DEFAULT 'XOF',
    commission_amount       DOUBLE PRECISION,
    net_amount              DOUBLE PRECISION,
    status                  VARCHAR(20)  DEFAULT 'PENDING',
    provider_name           VARCHAR(50),
    provider_transaction_id VARCHAR(200),
    paid_at                 TIMESTAMP,
    released_at             TIMESTAMP,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_contract ON td_payments(contract_id);
CREATE INDEX IF NOT EXISTS idx_payments_payer    ON td_payments(payer_id);
CREATE INDEX IF NOT EXISTS idx_payments_payee    ON td_payments(payee_id);
CREATE INDEX IF NOT EXISTS idx_payments_status   ON td_payments(status);

-- ---------------------------------------------------------------------
-- Conversations & Messages
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_conversations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user1_id        UUID NOT NULL REFERENCES td_users(id),
    user2_id        UUID NOT NULL REFERENCES td_users(id),
    project_id      UUID REFERENCES td_projects(id) ON DELETE SET NULL,
    type            VARCHAR(20)  DEFAULT 'DIRECT',
    last_message_at TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user1_id, user2_id)
);

CREATE INDEX IF NOT EXISTS idx_conversations_user1 ON td_conversations(user1_id);
CREATE INDEX IF NOT EXISTS idx_conversations_user2 ON td_conversations(user2_id);

CREATE TABLE IF NOT EXISTS td_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES td_conversations(id) ON DELETE CASCADE,
    sender_id       UUID NOT NULL REFERENCES td_users(id),
    content         TEXT,
    attachment_url  TEXT,
    is_read         BOOLEAN      DEFAULT FALSE,
    sent_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at         TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation ON td_messages(conversation_id);

-- ---------------------------------------------------------------------
-- Notifications
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_notifications (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL REFERENCES td_users(id) ON DELETE CASCADE,
    notification_type VARCHAR(30)  NOT NULL,
    message           VARCHAR(500) NOT NULL,
    entity_id         UUID,
    is_read           BOOLEAN      DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at           TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON td_notifications(user_id);

-- ---------------------------------------------------------------------
-- Reports (Moderation)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_reports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id     UUID NOT NULL REFERENCES td_users(id),
    report_type     VARCHAR(20)  NOT NULL,
    target_id       UUID NOT NULL,
    reason          VARCHAR(30)  NOT NULL,
    description     TEXT,
    status          VARCHAR(20)  DEFAULT 'PENDING',
    resolved_by_id  UUID REFERENCES td_users(id),
    resolution_note TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at     TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reports_reporter ON td_reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_reports_status   ON td_reports(status);

-- ---------------------------------------------------------------------
-- Seed default categories
-- ---------------------------------------------------------------------
INSERT INTO td_categories (name, description, icon_name, is_active, display_order)
VALUES
    ('Développement Web',  'Sites web, applications web, APIs',          'code',        TRUE, 1),
    ('Développement Mobile','Applications iOS, Android, cross-platform',  'smartphone',  TRUE, 2),
    ('Design & UI/UX',     'Logos, interfaces, design systems',          'palette',     TRUE, 3),
    ('Data & IA',          'Data science, machine learning, analytics',   'database',    TRUE, 4),
    ('Marketing Digital',  'SEO, social media, content marketing',        'trending-up', TRUE, 5),
    ('Rédaction & Traduction', 'Articles, copywriting, traduction',       'pen',         TRUE, 6),
    ('Vidéo & Animation',  'Montage, motion design, animation',           'video',       TRUE, 7),
    ('Comptabilité & Finance', 'Comptabilité, audit, conseil financier',  'calculator',  TRUE, 8),
    ('Conseil & Business', 'Stratégie, management, juridique',            'briefcase',   TRUE, 9),
    ('Autres',             'Toutes les autres missions',                  'more',        TRUE, 10)
ON CONFLICT (name) DO NOTHING;
