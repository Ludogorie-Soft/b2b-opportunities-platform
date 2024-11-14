UPDATE experience
SET months = (years * 12) + months;

ALTER TABLE experience
DROP COLUMN years;