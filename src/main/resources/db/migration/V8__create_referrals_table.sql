CREATE TABLE referrals (
    id BIGSERIAL PRIMARY KEY,
    referrer_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    referred_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    referral_code VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reward_months INTEGER NOT NULL DEFAULT 1,
    reward_applied BOOLEAN NOT NULL DEFAULT FALSE,
    reward_applied_at TIMESTAMP,
    referred_email VARCHAR(255),
    converted_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_referrals_referrer_user_id ON referrals(referrer_user_id);
CREATE INDEX idx_referrals_referral_code ON referrals(referral_code);
CREATE INDEX idx_referrals_status ON referrals(status);
