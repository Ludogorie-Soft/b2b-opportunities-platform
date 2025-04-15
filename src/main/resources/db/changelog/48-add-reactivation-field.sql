ALTER TABLE projects
ADD COLUMN can_reactivate BOOLEAN;

UPDATE projects
SET can_reactivate = false
WHERE can_reactivate IS NULL;

ALTER TABLE projects
ALTER COLUMN can_reactivate SET NOT NULL;