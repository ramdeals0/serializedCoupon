-- Serialized coupon schema (PostgreSQL).
-- Timestamps are stored as timestamptz and treated as UTC.

CREATE TABLE rms_coupon_definition (
    id UUID PRIMARY KEY,
    rms_coupon_id VARCHAR(64) NOT NULL,
    rms_coupon_code VARCHAR(64),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_rms_coupon_definition_rms_coupon_id UNIQUE (rms_coupon_id)
);

CREATE TABLE coupon_batch (
    id UUID PRIMARY KEY,
    rms_coupon_definition_id UUID NOT NULL REFERENCES rms_coupon_definition (id),
    coupon_program_code VARCHAR(4) NOT NULL,
    requested_quantity INTEGER NOT NULL,
    generated_quantity INTEGER NOT NULL DEFAULT 0,
    start_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by VARCHAR(128),
    idempotency_key VARCHAR(128),
    request_fingerprint VARCHAR(64),
    external_reference VARCHAR(128),
    error_message VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_coupon_batch_program_code CHECK (coupon_program_code ~ '^[0-9]{4}$'),
    CONSTRAINT chk_coupon_batch_dates CHECK (start_at < expires_at),
    CONSTRAINT chk_coupon_batch_quantity CHECK (requested_quantity >= 1),
    CONSTRAINT uq_coupon_batch_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_coupon_batch_rms_coupon_definition_id ON coupon_batch (rms_coupon_definition_id);
CREATE INDEX idx_coupon_batch_created_at ON coupon_batch (created_at);

CREATE TABLE serialized_coupon (
    id UUID PRIMARY KEY,
    coupon_batch_id UUID NOT NULL REFERENCES coupon_batch (id),
    rms_coupon_definition_id UUID NOT NULL REFERENCES rms_coupon_definition (id),
    coupon_code VARCHAR(14) NOT NULL,
    coupon_program_code VARCHAR(4) NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    redeemed_at TIMESTAMPTZ,
    deactivated_at TIMESTAMPTZ,
    external_reference VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_serialized_coupon_code UNIQUE (coupon_code),
    CONSTRAINT chk_serialized_coupon_code_format CHECK (coupon_code ~ '^FF[0-9]{4}[A-Z0-9]{8}$'),
    CONSTRAINT chk_serialized_coupon_program_code CHECK (coupon_program_code ~ '^[0-9]{4}$'),
    CONSTRAINT chk_serialized_coupon_dates CHECK (start_at < expires_at)
);

CREATE INDEX idx_serialized_coupon_rms_coupon_definition_id ON serialized_coupon (rms_coupon_definition_id);
CREATE INDEX idx_serialized_coupon_coupon_batch_id ON serialized_coupon (coupon_batch_id);
CREATE INDEX idx_serialized_coupon_status ON serialized_coupon (status);
CREATE INDEX idx_serialized_coupon_start_at ON serialized_coupon (start_at);
CREATE INDEX idx_serialized_coupon_expires_at ON serialized_coupon (expires_at);
