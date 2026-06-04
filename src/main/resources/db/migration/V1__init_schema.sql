-- =============================================
-- SellerPro - Complete Database Schema V1
-- Includes: Users, Plans, Subscriptions, Orders, Alerts, Referrals, Logs
-- =============================================

-- ENUM types
CREATE TYPE platform_type AS ENUM ('AMAZON','FLIPKART','MEESHO','NYKAA','WEBSITE');
CREATE TYPE plan_name_type AS ENUM ('BASIC','PRO','ADMIN');
CREATE TYPE subscription_status AS ENUM ('ACTIVE','EXPIRED','CANCELLED','TRIAL');
CREATE TYPE payment_status AS ENUM ('PENDING','SUCCESS','FAILED','REFUNDED');

-- =============================================
-- PLANS TABLE
-- =============================================
CREATE TABLE plans (
    id                  BIGSERIAL PRIMARY KEY,
    name                plan_name_type NOT NULL UNIQUE,
    display_name        VARCHAR(50) NOT NULL,
    price               NUMERIC(10,2) NOT NULL DEFAULT 0,
    duration_months     INT NOT NULL DEFAULT 1,
    max_platforms       INT NOT NULL DEFAULT 1,
    max_months_history  INT NOT NULL DEFAULT 3,    -- -1 = unlimited
    whatsapp_digest     BOOLEAN NOT NULL DEFAULT FALSE,
    analytics_access    BOOLEAN NOT NULL DEFAULT FALSE,
    gst_report_v2       BOOLEAN NOT NULL DEFAULT FALSE,
    smart_alerts        BOOLEAN NOT NULL DEFAULT FALSE,
    seller_score        BOOLEAN NOT NULL DEFAULT FALSE,
    referral_program    BOOLEAN NOT NULL DEFAULT FALSE,
    allowed_platforms   TEXT NOT NULL DEFAULT 'AMAZON',  -- comma separated
    description         TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- USERS TABLE
-- =============================================
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    business_name   VARCHAR(255),
    gst_number      VARCHAR(20),
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',  -- USER, ADMIN
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    referral_code   VARCHAR(20) UNIQUE,
    referred_by_id  BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMP
);

-- =============================================
-- SUBSCRIPTIONS TABLE
-- =============================================
CREATE TABLE subscriptions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id         BIGINT NOT NULL REFERENCES plans(id),
    status          subscription_status NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date        DATE NOT NULL,
    is_trial        BOOLEAN NOT NULL DEFAULT FALSE,
    notes           TEXT,
    created_by_id   BIGINT REFERENCES users(id),  -- admin who manually assigned
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- PAYMENTS TABLE
-- =============================================
CREATE TABLE payments (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    plan_id             BIGINT NOT NULL REFERENCES plans(id),
    razorpay_order_id   VARCHAR(100) UNIQUE,
    razorpay_payment_id VARCHAR(100) UNIQUE,
    amount              NUMERIC(10,2) NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'INR',
    status              payment_status NOT NULL DEFAULT 'PENDING',
    failure_reason      TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- CLIENTS TABLE (seller profiles)
-- =============================================
CREATE TABLE clients (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_name   VARCHAR(255),
    gst_number      VARCHAR(20),
    pan_number      VARCHAR(20),
    address         TEXT,
    state           VARCHAR(100),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- ORDERS TABLE
-- =============================================
CREATE TABLE orders (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform            platform_type NOT NULL,
    order_id            VARCHAR(255) NOT NULL,
    order_date          DATE NOT NULL,
    sku                 VARCHAR(255),
    product_name        TEXT,
    category            VARCHAR(255),
    quantity            INT NOT NULL DEFAULT 1,
    sale_price          NUMERIC(12,2),
    mrp                 NUMERIC(12,2),
    commission          NUMERIC(12,2) DEFAULT 0,
    tds                 NUMERIC(12,2) DEFAULT 0,
    shipping_fee        NUMERIC(12,2) DEFAULT 0,
    other_deductions    NUMERIC(12,2) DEFAULT 0,
    net_settlement      NUMERIC(12,2),
    status              VARCHAR(50),
    city                VARCHAR(255),
    state               VARCHAR(255),
    return_flag         BOOLEAN NOT NULL DEFAULT FALSE,
    upload_batch_id     VARCHAR(100),
    raw_data            JSONB,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- FILE UPLOADS TABLE
-- =============================================
CREATE TABLE file_uploads (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    platform        platform_type NOT NULL,
    original_name   VARCHAR(500) NOT NULL,
    s3_key          VARCHAR(500),
    file_size       BIGINT,
    rows_parsed     INT DEFAULT 0,
    rows_failed     INT DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'PROCESSING',  -- PROCESSING, DONE, FAILED
    error_message   TEXT,
    batch_id        VARCHAR(100) UNIQUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP
);

-- =============================================
-- ALERTS TABLE
-- =============================================
CREATE TABLE alerts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(50) NOT NULL,   -- RETURN_SPIKE, LOW_STOCK, REVENUE_DROP etc
    platform        platform_type,
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    severity        VARCHAR(20) NOT NULL DEFAULT 'INFO',  -- INFO, WARNING, CRITICAL
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- REFERRALS TABLE
-- =============================================
CREATE TABLE referrals (
    id                  BIGSERIAL PRIMARY KEY,
    referrer_id         BIGINT NOT NULL REFERENCES users(id),
    referred_id         BIGINT NOT NULL REFERENCES users(id),
    bonus_months_given  INT NOT NULL DEFAULT 0,
    rewarded_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- ACTIVITY LOGS TABLE
-- =============================================
CREATE TABLE activity_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id),
    action      VARCHAR(100) NOT NULL,   -- LOGIN, LOGOUT, FILE_UPLOAD, PLAN_CHANGE etc
    entity_type VARCHAR(50),
    entity_id   BIGINT,
    details     JSONB,
    ip_address  VARCHAR(50),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- WHATSAPP DIGEST SUBSCRIPTIONS
-- =============================================
CREATE TABLE whatsapp_configs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    phone           VARCHAR(20) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    frequency       VARCHAR(20) NOT NULL DEFAULT 'DAILY',  -- DAILY, WEEKLY
    last_sent_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- INDEXES
-- =============================================
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_platform ON orders(platform);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_orders_sku ON orders(sku);
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_alerts_user_id ON alerts(user_id);
CREATE INDEX idx_alerts_is_read ON alerts(is_read);
CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_file_uploads_user_id ON file_uploads(user_id);

-- =============================================
-- SEED DATA - Default Plans
-- =============================================
INSERT INTO plans (name, display_name, price, duration_months, max_platforms, max_months_history,
                   whatsapp_digest, analytics_access, gst_report_v2, smart_alerts,
                   seller_score, referral_program, allowed_platforms, description)
VALUES
('BASIC', 'Basic Plan', 499.00, 1, 1, 3,
 FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
 'AMAZON',
 'Perfect for Amazon-only sellers. Basic dashboard, revenue overview, CSV export.'),

('PRO', 'Pro Plan', 999.00, 1, 5, -1,
 TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
 'AMAZON,FLIPKART,MEESHO,NYKAA,WEBSITE',
 'Full access — all platforms, advanced analytics, WhatsApp digest, GST reports, smart alerts.'),

('ADMIN', 'Admin (Internal)', 0.00, 120, 5, -1,
 TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
 'AMAZON,FLIPKART,MEESHO,NYKAA,WEBSITE',
 'Internal admin plan — unlimited everything.');

-- =============================================
-- SEED DATA - Default Admin User
-- Password: Admin@123 (BCrypt hash)
-- =============================================
INSERT INTO users (email, password_hash, full_name, role, is_active, email_verified, referral_code)
VALUES ('admin@sellerpro.in',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        'SellerPro Admin', 'ADMIN', TRUE, TRUE, 'ADMIN001');
