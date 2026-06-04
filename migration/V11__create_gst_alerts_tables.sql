-- V11__create_gst_alerts_tables.sql

-- GST Entries table
CREATE TABLE IF NOT EXISTS gst_entries (
    id              BIGSERIAL PRIMARY KEY,
    client_id       BIGINT NOT NULL,
    order_id        VARCHAR(100) NOT NULL,
    platform        VARCHAR(50),
    order_date      DATE,
    buyer_state     VARCHAR(100),
    taxable_amount  NUMERIC(14, 2),
    cgst            NUMERIC(14, 2),
    sgst            NUMERIC(14, 2),
    igst            NUMERIC(14, 2),
    total_gst       NUMERIC(14, 2),
    gst_rate        VARCHAR(10),
    hsn_code        VARCHAR(20),
    invoice_number  VARCHAR(100),
    return_period   VARCHAR(7) NOT NULL,  -- format: YYYY-MM
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_gst_client_order UNIQUE (client_id, order_id)
);

CREATE INDEX idx_gst_client_period ON gst_entries (client_id, return_period);
CREATE INDEX idx_gst_client_date   ON gst_entries (client_id, order_date);

-- Alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id          BIGSERIAL PRIMARY KEY,
    client_id   BIGINT NOT NULL,
    type        VARCHAR(50)  NOT NULL,   -- RETURN_SPIKE, REVENUE_DIP, HIGH_RETURNS
    severity    VARCHAR(20)  NOT NULL,   -- INFO, WARNING, CRITICAL
    title       VARCHAR(255) NOT NULL,
    message     TEXT,
    platform    VARCHAR(50),
    value       NUMERIC(14, 2),
    threshold   NUMERIC(14, 2),
    read        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alerts_client_id        ON alerts (client_id);
CREATE INDEX idx_alerts_client_unread    ON alerts (client_id, read) WHERE read = FALSE;
CREATE INDEX idx_alerts_client_created   ON alerts (client_id, created_at DESC);

-- Add referral fields to clients table (if not exists)
ALTER TABLE clients ADD COLUMN IF NOT EXISTS referral_code VARCHAR(20) UNIQUE;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS referred_by   VARCHAR(20);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS whatsapp      VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_clients_referral_code ON clients (referral_code);
