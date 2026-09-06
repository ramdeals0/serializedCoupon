CREATE TABLE coupon (
    id UUID PRIMARY KEY,
    rms_coupon_definition_id UUID NOT NULL REFERENCES rms_coupon_definition (id),
    coupon_program_code VARCHAR(4) NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by VARCHAR(128),
    external_reference VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_coupon_program_code CHECK (coupon_program_code ~ '^[0-9]{4}$'),
    CONSTRAINT chk_coupon_dates CHECK (start_at < expires_at)
);

CREATE INDEX idx_coupon_rms_coupon_definition_id ON coupon (rms_coupon_definition_id);
CREATE INDEX idx_coupon_created_at ON coupon (created_at);

INSERT INTO coupon (
    id,
    rms_coupon_definition_id,
    coupon_program_code,
    start_at,
    expires_at,
    status,
    created_by,
    external_reference,
    created_at,
    updated_at
)
SELECT
    id,
    rms_coupon_definition_id,
    coupon_program_code,
    start_at,
    expires_at,
    'ACTIVE',
    created_by,
    external_reference,
    created_at,
    updated_at
FROM coupon_batch;

ALTER TABLE coupon_batch ADD COLUMN coupon_id UUID REFERENCES coupon (id);
UPDATE coupon_batch SET coupon_id = id;
CREATE INDEX idx_coupon_batch_coupon_id ON coupon_batch (coupon_id);
