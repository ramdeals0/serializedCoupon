ALTER TABLE coupon ADD COLUMN title VARCHAR(255);
ALTER TABLE coupon ADD COLUMN description TEXT;
ALTER TABLE coupon ADD COLUMN usage_limit INTEGER;
ALTER TABLE coupon ADD COLUMN pos_code VARCHAR(64);
ALTER TABLE coupon ADD COLUMN atg_code VARCHAR(64);
ALTER TABLE coupon ADD COLUMN coupon_source VARCHAR(16);

UPDATE coupon c
SET title = COALESCE(
            (SELECT r.name FROM rms_coupon_definition r WHERE r.id = c.rms_coupon_definition_id),
            'Coupon'
        ),
    description = (SELECT r.description FROM rms_coupon_definition r WHERE r.id = c.rms_coupon_definition_id),
    usage_limit = 1,
    pos_code = (SELECT r.rms_coupon_code FROM rms_coupon_definition r WHERE r.id = c.rms_coupon_definition_id),
    coupon_source = 'BOTH'
WHERE c.title IS NULL;

ALTER TABLE coupon ALTER COLUMN title SET NOT NULL;
ALTER TABLE coupon ALTER COLUMN usage_limit SET NOT NULL;
ALTER TABLE coupon ALTER COLUMN coupon_source SET NOT NULL;

ALTER TABLE coupon ADD CONSTRAINT chk_coupon_usage_limit CHECK (usage_limit >= 1);
ALTER TABLE coupon ADD CONSTRAINT chk_coupon_source CHECK (coupon_source IN ('POS', 'ECOMM', 'BOTH'));
