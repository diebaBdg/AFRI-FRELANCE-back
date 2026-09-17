-- =====================================================================
-- AfriFreelance — Initial schema
-- =====================================================================

-- ---------------------------------------------------------------------
-- Roles & Permissions
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_roles (
    id                BIGSERIAL    PRIMARY KEY,
    code              VARCHAR(50)  NOT NULL UNIQUE,
    libelle           VARCHAR(100) NOT NULL,
    description       TEXT,
    niveau_autorisation INTEGER,
    actif             BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_permissions (
    id              BIGSERIAL    PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    libelle         VARCHAR(200) NOT NULL,
    description     TEXT,
    module          VARCHAR(100),
    actif           BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_role_permissions (
    id                BIGSERIAL PRIMARY KEY,
    role_id           BIGINT    NOT NULL REFERENCES td_roles(id),
    permission_id     BIGINT    NOT NULL REFERENCES td_permissions(id),
    date_assignation  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (role_id, permission_id)
);

-- ---------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_users (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username              VARCHAR(50)  NOT NULL UNIQUE,
    email                 VARCHAR(100) NOT NULL UNIQUE,
    full_name             VARCHAR(200) NOT NULL,
    adresse               VARCHAR(255),
    phone                 VARCHAR(20),
    password              TEXT         NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'ACTIF',
    active_mode           VARCHAR(20)  DEFAULT 'FREELANCE',
    avatar_url            TEXT,
    headline              VARCHAR(200),
    bio                   TEXT,
    country               VARCHAR(100),
    city                  VARCHAR(100),
    default_currency      VARCHAR(10)  DEFAULT 'XOF',
    timezone              VARCHAR(50)  DEFAULT 'Africa/Dakar',
    primary_language      VARCHAR(50)  DEFAULT 'Français',
    member_since_year     INTEGER,
    last_password_change  TIMESTAMP,
    total_assigned        INTEGER      DEFAULT 0,
    total_approved        INTEGER      DEFAULT 0,
    total_rejected        INTEGER      DEFAULT 0,
    date_creation         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_user_roles (
    user_id  UUID   NOT NULL REFERENCES td_users(id),
    role_id  BIGINT NOT NULL REFERENCES td_roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- ---------------------------------------------------------------------
-- Freelance profiles and related sections
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_freelance_profiles (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL UNIQUE REFERENCES td_users(id),
    title                 VARCHAR(200),
    overview              TEXT,
    country               VARCHAR(100),
    city                  VARCHAR(100),
    hourly_rate           DOUBLE PRECISION,
    daily_rate            DOUBLE PRECISION,
    min_project_rate      DOUBLE PRECISION,
    currency              VARCHAR(10)  DEFAULT 'XOF',
    availability_status   VARCHAR(20)  DEFAULT 'AVAILABLE',
    availability_type     VARCHAR(20)  DEFAULT 'FULL_TIME',
    weekly_hours          INTEGER,
    profile_active        BOOLEAN      DEFAULT TRUE,
    completion_percentage INTEGER       DEFAULT 0,
    is_verified           BOOLEAN      DEFAULT FALSE,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_skills (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    freelance_profile_id  UUID NOT NULL REFERENCES td_freelance_profiles(id) ON DELETE CASCADE,
    name                  VARCHAR(100) NOT NULL,
    level                 VARCHAR(20)  DEFAULT 'INTERMEDIATE',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_work_experiences (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    freelance_profile_id  UUID NOT NULL REFERENCES td_freelance_profiles(id) ON DELETE CASCADE,
    job_title             VARCHAR(200) NOT NULL,
    company               VARCHAR(200) NOT NULL,
    country               VARCHAR(100),
    start_date            DATE         NOT NULL,
    end_date              DATE,
    current               BOOLEAN      DEFAULT FALSE,
    description           TEXT,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_educations (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    freelance_profile_id  UUID NOT NULL REFERENCES td_freelance_profiles(id) ON DELETE CASCADE,
    institution           VARCHAR(200) NOT NULL,
    degree                VARCHAR(200) NOT NULL,
    field                 VARCHAR(200),
    start_date            DATE         NOT NULL,
    end_date              DATE,
    description           TEXT,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_certifications (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    freelance_profile_id  UUID NOT NULL REFERENCES td_freelance_profiles(id) ON DELETE CASCADE,
    name                  VARCHAR(200) NOT NULL,
    issuer                VARCHAR(200),
    issue_date            DATE,
    verification_url      VARCHAR(500),
    credential_id         VARCHAR(200),
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_languages (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    freelance_profile_id  UUID NOT NULL REFERENCES td_freelance_profiles(id) ON DELETE CASCADE,
    language_name         VARCHAR(100) NOT NULL,
    level                 VARCHAR(20)  DEFAULT 'INTERMEDIAIRE',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_portfolio_items (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    freelance_profile_id  UUID NOT NULL REFERENCES td_freelance_profiles(id) ON DELETE CASCADE,
    title                 VARCHAR(200) NOT NULL,
    description           TEXT,
    image_url              TEXT,
    project_url            VARCHAR(500),
    technologies           TEXT[],
    project_date           DATE,
    role                   VARCHAR(200),
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_professional_links (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    freelance_profile_id  UUID NOT NULL REFERENCES td_freelance_profiles(id) ON DELETE CASCADE,
    platform              VARCHAR(20)  NOT NULL,
    url                   VARCHAR(500) NOT NULL,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_work_preferences (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    freelance_profile_id  UUID NOT NULL UNIQUE REFERENCES td_freelance_profiles(id) ON DELETE CASCADE,
    work_types            TEXT[],
    project_types         TEXT[],
    accepted_countries    TEXT[],
    timezone              VARCHAR(50),
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Client profiles
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_client_profiles (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL UNIQUE REFERENCES td_users(id),
    display_name        VARCHAR(200),
    client_type         VARCHAR(20)  DEFAULT 'INDIVIDUAL',
    company_id          UUID,
    overview            TEXT,
    country             VARCHAR(100),
    city                VARCHAR(100),
    profile_active      BOOLEAN      DEFAULT TRUE,
    projects_published  INTEGER      DEFAULT 0,
    projects_completed  INTEGER      DEFAULT 0,
    hire_rate           DOUBLE PRECISION DEFAULT 0.0,
    avg_rating          DOUBLE PRECISION DEFAULT 0.0,
    total_spent         DOUBLE PRECISION DEFAULT 0.0,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Companies
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_companies (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id        UUID NOT NULL REFERENCES td_users(id),
    legal_name      VARCHAR(200),
    commercial_name VARCHAR(200),
    sector          VARCHAR(100),
    size            VARCHAR(20)  DEFAULT 'SOLO',
    country         VARCHAR(100),
    city            VARCHAR(100),
    website         VARCHAR(500),
    description     TEXT,
    logo_url        TEXT,
    is_verified     BOOLEAN      DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS td_company_members (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES td_companies(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES td_users(id),
    role        VARCHAR(20) DEFAULT 'MEMBER',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (company_id, user_id)
);

-- ---------------------------------------------------------------------
-- Verifications & Badges
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_verifications (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID NOT NULL REFERENCES td_users(id) ON DELETE CASCADE,
    verification_type  VARCHAR(20) NOT NULL,
    status             VARCHAR(20)  DEFAULT 'PENDING',
    verified_at        TIMESTAMP,
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, verification_type)
);

CREATE TABLE IF NOT EXISTS td_badges (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES td_users(id) ON DELETE CASCADE,
    badge_type  VARCHAR(50) NOT NULL,
    awarded_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, badge_type)
);

-- ---------------------------------------------------------------------
-- Favorites
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_favorites (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES td_users(id) ON DELETE CASCADE,
    target_user_id  UUID NOT NULL REFERENCES td_users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, target_user_id)
);

-- ---------------------------------------------------------------------
-- Notification preferences
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS td_notification_preferences (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL UNIQUE REFERENCES td_users(id) ON DELETE CASCADE,
    email_messages           BOOLEAN DEFAULT TRUE,
    email_project_updates    BOOLEAN DEFAULT TRUE,
    email_marketing          BOOLEAN DEFAULT FALSE,
    push_messages            BOOLEAN DEFAULT TRUE,
    push_project_updates     BOOLEAN DEFAULT TRUE,
    push_contract_updates    BOOLEAN DEFAULT TRUE,
    push_payment_updates     BOOLEAN DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_freelance_profiles_user        ON td_freelance_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_client_profiles_user          ON td_client_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_company_members_company       ON td_company_members(company_id);
CREATE INDEX IF NOT EXISTS idx_company_members_user          ON td_company_members(user_id);
CREATE INDEX IF NOT EXISTS idx_skills_profile                ON td_skills(freelance_profile_id);
CREATE INDEX IF NOT EXISTS idx_work_exp_profile              ON td_work_experiences(freelance_profile_id);
CREATE INDEX IF NOT EXISTS idx_educations_profile            ON td_educations(freelance_profile_id);
CREATE INDEX IF NOT EXISTS idx_certifications_profile        ON td_certifications(freelance_profile_id);
CREATE INDEX IF NOT EXISTS idx_languages_profile            ON td_languages(freelance_profile_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_profile            ON td_portfolio_items(freelance_profile_id);
CREATE INDEX IF NOT EXISTS idx_prof_links_profile           ON td_professional_links(freelance_profile_id);
CREATE INDEX IF NOT EXISTS idx_verifications_user            ON td_verifications(user_id);
CREATE INDEX IF NOT EXISTS idx_badges_user                   ON td_badges(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_user               ON td_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_target              ON td_favorites(target_user_id);
CREATE INDEX IF NOT EXISTS idx_companies_owner              ON td_companies(owner_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role               ON td_user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_role         ON td_role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission   ON td_role_permissions(permission_id);
