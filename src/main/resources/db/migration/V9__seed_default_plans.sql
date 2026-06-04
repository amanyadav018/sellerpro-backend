-- Insert default plans
INSERT INTO plans (name, display_name, price, duration_months, max_months_history, max_platforms,
    whatsapp_digest, analytics_access, gst_report_v2, smart_alerts, csv_export, referral_program)
VALUES
('BASIC', 'Basic Plan', 499.00, 1, 3, 1, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
('PRO', 'Pro Plan', 999.00, 1, NULL, 5, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE);

-- Insert Basic plan features
INSERT INTO plan_features (plan_id, feature_key, feature_value, is_enabled) VALUES
((SELECT id FROM plans WHERE name = 'BASIC'), 'AMAZON_PARSER', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'BASIC'), 'BASIC_DASHBOARD', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'BASIC'), 'CSV_EXPORT', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'BASIC'), 'HISTORY_MONTHS', '3', TRUE),
((SELECT id FROM plans WHERE name = 'BASIC'), 'CITY_HEATMAP', 'false', FALSE),
((SELECT id FROM plans WHERE name = 'BASIC'), 'SKU_RANKING', 'false', FALSE),
((SELECT id FROM plans WHERE name = 'BASIC'), 'WHATSAPP_DIGEST', 'false', FALSE),
((SELECT id FROM plans WHERE name = 'BASIC'), 'GST_REPORT_V2', 'false', FALSE);

-- Insert Basic plan platforms
INSERT INTO plan_allowed_platforms (plan_id, platform) VALUES
((SELECT id FROM plans WHERE name = 'BASIC'), 'AMAZON');

-- Insert Pro plan features
INSERT INTO plan_features (plan_id, feature_key, feature_value, is_enabled) VALUES
((SELECT id FROM plans WHERE name = 'PRO'), 'AMAZON_PARSER', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'FLIPKART_PARSER', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'MEESHO_PARSER', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'NYKAA_PARSER', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'WEBSITE_PARSER', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'BASIC_DASHBOARD', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'FULL_ANALYTICS', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'CITY_HEATMAP', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'SKU_RANKING', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'WHATSAPP_DIGEST', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'GST_REPORT_V2', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'SMART_ALERTS', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'CSV_EXPORT', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'REFERRAL_PROGRAM', 'true', TRUE),
((SELECT id FROM plans WHERE name = 'PRO'), 'HISTORY_MONTHS', 'unlimited', TRUE);

-- Insert Pro plan platforms
INSERT INTO plan_allowed_platforms (plan_id, platform) VALUES
((SELECT id FROM plans WHERE name = 'PRO'), 'AMAZON'),
((SELECT id FROM plans WHERE name = 'PRO'), 'FLIPKART'),
((SELECT id FROM plans WHERE name = 'PRO'), 'MEESHO'),
((SELECT id FROM plans WHERE name = 'PRO'), 'NYKAA'),
((SELECT id FROM plans WHERE name = 'PRO'), 'WEBSITE');

-- Insert default admin user (password: Admin@123 - BCrypt hashed)
INSERT INTO users (email, password, full_name, role, is_active, email_verified)
VALUES ('admin@sellerpro.in',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCMf/wWvExAQJFjZGDcHXNS',
        'SellerPro Admin', 'ADMIN', TRUE, TRUE);
