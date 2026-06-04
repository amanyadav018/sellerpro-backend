CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(255),
    gstin VARCHAR(15),
    pan VARCHAR(10),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(6),
    amazon_seller_id VARCHAR(100),
    flipkart_seller_id VARCHAR(100),
    meesho_seller_id VARCHAR(100),
    nykaa_seller_id VARCHAR(100),
    website_url VARCHAR(500),
    logo_url VARCHAR(500),
    onboarding_complete BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clients_user_id ON clients(user_id);
CREATE INDEX idx_clients_gstin ON clients(gstin);
