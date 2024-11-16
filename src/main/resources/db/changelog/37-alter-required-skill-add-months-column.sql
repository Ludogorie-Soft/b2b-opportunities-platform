ALTER TABLE required_skill
ADD COLUMN months INTEGER;

UPDATE required_skill rs
SET months = e.months
FROM experience e
WHERE rs.experience_id = e.id;

ALTER TABLE required_skill
DROP COLUMN experience_id;
