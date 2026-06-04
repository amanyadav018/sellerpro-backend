CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    platform VARCHAR(30) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    order_date DATE NOT NULL,
    sku VARCHAR(255),
    product_name TEXT,
    category VARCHAR(255),
    quantity INTEGER NOT NULL DEFAULT 1,
    selling_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    mrp DECIMAL(12,2),
    discount DECIMAL(12,2) DEFAULT 0,
    marketplace_fee DECIMAL(12,2) DEFAULT 0,
    shipping_fee DECIMAL(12,2) DEFAULT 0,
    commission DECIMAL(12,2) DEFAULT 0,
    tcs_amount DECIMAL(12,2) DEFAULT 0,
    tds_amount DECIMAL(12,2) DEFAULT 0,
    settlement_amount DECIMAL(12,2) DEFAULT 0,
    net_revenue DECIMAL(12,2) DEFAULT 0,
    order_status VARCHAR(50),
    return_status VARCHAR(50),
    return_date DATE,
    return_reason TEXT,
    buyer_city VARCHAR(100),
    buyer_state VARCHAR(100),
    buyer_pincode VARCHAR(10),
    payment_mode VARCHAR(50),
    invoice_number VARCHAR(255),
    batch_id VARCHAR(255),
    raw_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_client_id ON orders(client_id);
CREATE INDEX idx_orders_platform ON orders(platform);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_orders_sku ON orders(sku);
CREATE INDEX idx_orders_order_status ON orders(order_status);
CREATE INDEX idx_orders_batch_id ON orders(batch_id);
CREATE UNIQUE INDEX idx_orders_platform_order_id ON orders(client_id, platform, order_id);
