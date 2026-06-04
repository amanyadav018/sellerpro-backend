CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES plans(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    payment_reference VARCHAR(255),
    payment_amount DECIMAL(10,2),
    payment_method VARCHAR(50),
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    extended_by_admin BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_plan_id ON subscriptions(plan_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date);
