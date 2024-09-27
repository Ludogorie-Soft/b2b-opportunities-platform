CREATE TABLE domains (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO domains (name) VALUES
('Technology'),
('Fintech'),
('Automotive');