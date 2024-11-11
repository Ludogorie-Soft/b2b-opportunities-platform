ALTER TABLE companies
ADD COLUMN is_approved BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE users
DROP COLUMN is_approved;