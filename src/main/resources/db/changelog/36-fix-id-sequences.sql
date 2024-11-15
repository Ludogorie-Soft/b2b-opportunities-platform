SELECT setval('talent_experience_id_seq', (SELECT MAX(id) FROM talent_experience));

SELECT setval('skill_experience_id_seq', (SELECT MAX(id) FROM skill_experience));

SELECT setval('talents_id_seq', (SELECT MAX(id) FROM talents));
