ALTER TABLE talents ADD COLUMN min_rate INT;
ALTER TABLE talents ADD COLUMN max_rate INT;

UPDATE talents t
SET
    min_rate =
        CASE
            WHEN te.seniority_id IN (1, 2) THEN
                FLOOR((RANDOM() * (15 - 10 + 1) + 10) / 5) * 5
            WHEN te.seniority_id = 3 THEN
                FLOOR((RANDOM() * (25 - 15 + 1) + 15) / 5) * 5
            WHEN te.seniority_id = 4 THEN
                FLOOR((RANDOM() * (40 - 25 + 1) + 25) / 5) * 5
            WHEN te.seniority_id = 5 THEN
                FLOOR((RANDOM() * (70 - 40 + 1) + 40) / 5) * 5
            WHEN te.seniority_id = 6 THEN
                FLOOR((RANDOM() * (90 - 70 + 1) + 70) / 5) * 5
            WHEN te.seniority_id = 7 THEN
                FLOOR((RANDOM() * (110 - 95 + 1) + 95) / 5) * 5
            WHEN te.seniority_id = 8 THEN
                FLOOR((RANDOM() * (130 - 110 + 1) + 110) / 5) * 5
            ELSE 0
        END
FROM talent_experience te
WHERE t.id = te.talent_id
  AND t.min_rate IS NULL;

UPDATE talents t
SET
    max_rate =
        CASE
            WHEN te.seniority_id IN (1, 2) THEN
                FLOOR((RANDOM() * (25 - 15 + 1) + 15) / 5) * 5
            WHEN te.seniority_id = 3 THEN
                FLOOR((RANDOM() * (40 - 25 + 1) + 25) / 5) * 5
            WHEN te.seniority_id = 4 THEN
                FLOOR((RANDOM() * (60 - 40 + 1) + 40) / 5) * 5
            WHEN te.seniority_id = 5 THEN
                FLOOR((RANDOM() * (100 - 70 + 1) + 70) / 5) * 5
            WHEN te.seniority_id = 6 THEN
                FLOOR((RANDOM() * (120 - 90 + 1) + 90) / 5) * 5
            WHEN te.seniority_id = 7 THEN
                FLOOR((RANDOM() * (140 - 110 + 1) + 110) / 5) * 5
            WHEN te.seniority_id = 8 THEN
                FLOOR((RANDOM() * (150 - 130 + 1) + 130) / 5) * 5
            ELSE 0
        END
FROM talent_experience te
WHERE t.id = te.talent_id
  AND t.max_rate IS NULL
  AND t.id % 2 = 0;