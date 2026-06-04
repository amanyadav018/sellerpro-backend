CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_months INTEGER NOT NULL DEFAULT 1,
    max_months_history INTEGER,
    max_platforms INTEGER NOT NULL DEFAULT 1,
    whatsapp_digest BOOLEAN NOT NULL DEFAULT FALSE,
    analytics_access BOOLEAN NOT NULL DEFAULT FALSE,
    gst_report_v2 BOOLEAN NOT NULL DEFAULT FALSE,
    smart_alerts BOOLEAN NOT NULL DEFAULT FALSE,
    csv_export BOOLEAN NOT NULL DEFAULT TRUE,
    referral_program BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE plan_features (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    feature_key VARCHAR(100) NOT NULL,
    feature_value VARCHAR(255),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(plan_id, feature_key)
);

CREATE TABLE plan_allowed_platforms (
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    PRIMARY KEY (plan_id, platform)
);

CREATE INDEX idx_plans_name ON plans(name);
CREATE INDEX idx_plan_features_plan_id ON plan_features(plan_id);
