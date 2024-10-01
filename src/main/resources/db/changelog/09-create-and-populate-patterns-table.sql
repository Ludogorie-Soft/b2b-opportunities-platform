CREATE TABLE patterns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES patterns(id)
);

CREATE TABLE pattern_suggested_skills (
    pattern_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (pattern_id, skill_id),
    FOREIGN KEY (pattern_id) REFERENCES patterns(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

INSERT INTO patterns (id, name, parent_id) VALUES
(1, 'Full-Stack Developer', NULL),
(2, 'Front-End Developer', NULL),
(3, 'Back-End Developer', NULL),
(4, 'Mobile Developer', NULL),
(5, 'QA Engineer', NULL),
(6, 'Game Developer', NULL),
(7, 'UI/UX Designer', NULL),
(8, 'TypeScript Developer', 25),
(9, 'React Developer', 2),
(10, 'Java Developer', 3),
(11, 'Android Developer', 4),
(12, 'iOS Developer', 4),
(13, 'QA Manual Tester', 5),
(14, 'QA Automation Tester', 5),
(15, 'Python Developer', 3),
(16, 'Mobile Web Developer', 4),

(19, 'Database Administrator', 3),
(20, 'Cloud Engineer', NULL),
(21, 'DevOps Engineer', NULL),

(23, 'Data Scientist', NULL),
(24, 'Project Manager', NULL),
(25, 'Software Developer', NULL),
(26, '.NET Developer', 3),
(27, 'PHP Developer', 3),
(28, 'C# Developer', 3),
(29, 'WordPress Developer', 25),
(30, 'System Administrator', NULL),
(31, 'PowerBI Analyst', NULL),
(32, 'Angular Developer', 2),
(33, 'Rust Developer', 3),
(34, 'Go Developer', 3),
(35, 'Javascript Developer', 3);

INSERT INTO pattern_suggested_skills (pattern_id, skill_id) VALUES
-- For Pattern ID 2 (Front-End Developer)
(2, 51),
(2, 52),
(2, 53),
-- For Pattern ID 6 (Game Developer)
(6, 60),
(6, 61),
(6, 64),
(6, 82),
-- For Pattern ID 7 (UI/UX Designer)
(7, 103),
(7, 104),
-- For Pattern ID 8 (TypeScript Developer)
(8, 51),
(8, 52),
(8, 53),
(8, 56),
-- For Pattern ID 9 (React Developer)
(9, 51),
(9, 52),
(9, 53),
(9, 86),
-- For Pattern ID 10 (Java Developer)
(10, 57),
(10, 92),
-- For Pattern ID 11 (Android Developer)
(11, 58),
(11, 91),
-- For Pattern ID 12 (iOS Developer)
(12, 59),
(12, 87),
-- For Pattern ID 24 (Project Manager)
(24, 105),
(24, 106),
(24, 107),
-- For Pattern ID 26 (.NET Developer)
(26, 64),
(26, 95),
(26, 96),
-- For Pattern ID 27 (PHP Developer)
(27, 65),
(27, 84),
-- For Pattern ID 28 (C# Developer)
(28, 64),
-- For Pattern ID 29 (WordPress Developer)
(29, 132),
(29, 10),
-- For Pattern ID 30 (System Administrator)
(30, 135),
(30, 136),
(30, 137),
-- For Pattern ID 31 (PowerBI Analyst)
(31, 141),
-- For Pattern ID 32 (Angular Developer)
(32, 84),
(32, 51),
(32, 52),
(32, 53),
-- For Pattern ID 33 (Rust Developer)
(33, 67),
(33, 101),
-- For Pattern ID 34 (Go Developer)
(34, 68),
(34, 102),
-- For Pattern ID 35 (Javascript Developer)
(35, 51),
(35, 52),
(35, 97),
(35, 98);


SELECT setval('patterns_id_seq', (SELECT MAX(id) FROM patterns));

