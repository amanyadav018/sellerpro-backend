CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    platform VARCHAR(30),
    sku VARCHAR(255),
    metric_key VARCHAR(100),
    metric_value DECIMAL(15,2),
    threshold_value DECIMAL(15,2),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_dismissed BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    auto_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alerts_client_id ON alerts(client_id);
CREATE INDEX idx_alerts_alert_type ON alerts(alert_type);
CREATE INDEX idx_alerts_is_read ON alerts(is_read);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);
