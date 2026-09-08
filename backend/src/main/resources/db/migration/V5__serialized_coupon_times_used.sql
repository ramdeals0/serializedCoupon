ALTER TABLE serialized_coupon ADD COLUMN times_used INTEGER;
UPDATE serialized_coupon SET times_used = 0 WHERE times_used IS NULL;
