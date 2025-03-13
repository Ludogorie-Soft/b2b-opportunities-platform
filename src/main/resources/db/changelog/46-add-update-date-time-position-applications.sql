ALTER TABLE position_applications
ADD COLUMN last_update_date_time TIMESTAMP;

UPDATE position_applications
SET last_update_date_time = application_date_time
WHERE last_update_date_time IS NULL;

ALTER TABLE position_applications
ALTER COLUMN last_update_date_time SET NOT NULL;

ALTER TABLE position_applications
ALTER COLUMN last_update_date_time SET DEFAULT NOW();
