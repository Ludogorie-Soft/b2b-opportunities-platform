CREATE TABLE seniorities (
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    label VARCHAR(255) NOT NULL,
    level SMALLINT
);

INSERT INTO seniorities (id, label, level) VALUES
('SN1', 'Intern', '1'),
('SN2', 'Junior', '2'),
('SN3', 'Mid', '3'),
('SN4', 'Senior', '4'),
('SN5', 'Principal', '5'),
('SN6', 'Team Lead', '6'),
('SN7', 'Tech Lead', '7'),
('SN8', 'Engineering Lead', '8');