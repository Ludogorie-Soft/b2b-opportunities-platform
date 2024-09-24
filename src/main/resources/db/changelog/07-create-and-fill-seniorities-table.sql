CREATE TABLE seniorities (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(10) UNIQUE NOT NULL,
    label VARCHAR(255) NOT NULL,
    level SMALLINT
);

INSERT INTO seniorities (id, identifier, label, level) VALUES
(1 ,'SN1', 'Intern', 1),
(2 ,'SN2', 'Junior', 2),
(3 ,'SN3', 'Mid', 3),
(4 ,'SN4', 'Senior', 4),
(5 ,'SN5', 'Principal', 5),
(6 ,'SN6', 'Team Lead', 6),
(7 ,'SN7', 'Tech Lead', 7),
(8 ,'SN8', 'Engineering Lead', 8);