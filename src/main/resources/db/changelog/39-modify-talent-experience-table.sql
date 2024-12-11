ALTER TABLE talent_experience
DROP CONSTRAINT fk_talent_experience_talent;

ALTER TABLE talent_experience
ADD CONSTRAINT fk_talent_experience_talent
FOREIGN KEY (talent_id) REFERENCES talents(id)
ON DELETE CASCADE;