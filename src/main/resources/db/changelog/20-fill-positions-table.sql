INSERT INTO rates(min, max, currency) VALUES
(20, 33, 'EUR'),
(33, 53, 'EUR'),
(20, NULL, 'EUR'),
(40, 63, 'EUR'),
(16, 20, 'EUR'),
(26, 32, 'EUR'),
(23, 30, 'EUR'),
(42, 54, 'EUR');

INSERT INTO locations(name) VALUES
('Plovdiv'),
('Varna'),
('Sofia');

INSERT INTO experience(months, years) VALUES
(5,4),
(null,6),
(null,4),
(null,2),
(4,6),
(null,5),
(null,3),
(null,1);

INSERT INTO positions(project_id, pattern_id, is_active, seniority_id, rate_id, min_years_experience, location_id, hours_per_week) VALUES
(1, 9, TRUE, 4, 1, 2, 1),
(1, 3, FALSE, 3, 2, 3, NULL),
(1, 29, FALSE, 3, 3, NULL, NULL),
(2, 15, TRUE, 4, 4, NULL, NULL),
(3, 9, TRUE, 2, 5, 2, NULL),
(3, 9, TRUE, 4, 6, 3, 2),
(4, 30, TRUE, 3, 7, 4, NULL),
(4, 24, TRUE, 4, 8, 6, 3);

INSERT INTO position_work_modes(position_id, work_mode_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(3, 3),
(4, 2),
(4, 3),
(5, 3),
(6, 1),
(7, 3),
(8, 1);

INSERT INTO required_skill(position_id, skill_id, experience_id) VALUES
(1, 86, 1),
(2, 57, NULL),
(2, 64, NULL),
(3, 132, NULL),
(4, 62, 2),
(4, 43, 3),
(5, 51, 4),
(5, 86, 4),
(6, 86, 5),
(6, 22, 6),
(7, 18, 7),
(7, 26, 8),
(8, 107, 2),
(8, 105, 3),
(8, 106, 2);