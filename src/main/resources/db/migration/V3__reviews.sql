-- =====================================================================
-- AfriFreelance — Reviews table
-- =====================================================================

CREATE TABLE IF NOT EXISTS td_reviews (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reviewer_id UUID NOT NULL REFERENCES td_users(id) ON DELETE CASCADE,
    reviewed_id UUID NOT NULL REFERENCES td_users(id) ON DELETE CASCADE,
    rating      INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment     TEXT,
    project_id  UUID,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (reviewer_id, reviewed_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_reviewed ON td_reviews(reviewed_id);
CREATE INDEX IF NOT EXISTS idx_reviews_reviewer ON td_reviews(reviewer_id);
