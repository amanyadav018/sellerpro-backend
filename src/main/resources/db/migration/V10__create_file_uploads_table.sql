CREATE TABLE file_uploads (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    batch_id VARCHAR(255) UNIQUE,
    file_name VARCHAR(500) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    s3_key VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_rows INTEGER,
    success_count INTEGER,
    skip_count INTEGER,
    error_count INTEGER,
    error_log TEXT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_file_uploads_client_id ON file_uploads(client_id);
CREATE INDEX idx_file_uploads_platform ON file_uploads(platform);
CREATE INDEX idx_file_uploads_status ON file_uploads(status);
CREATE INDEX idx_file_uploads_uploaded_at ON file_uploads(uploaded_at DESC);
