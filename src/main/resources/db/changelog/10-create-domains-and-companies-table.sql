CREATE TABLE domains (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO domains (name) VALUES
('Technology'),
('Fintech'),
('Automotive');

CREATE TABLE company_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    website VARCHAR(255) NOT NULL,
    image VARCHAR(255) NOT NULL,
    email_verification VARCHAR(255),
    domain_id BIGINT,
    linked_in VARCHAR(255),
    banner VARCHAR(255),
    description TEXT,
    company_type_id BIGINT,
    CONSTRAINT fk_domain FOREIGN KEY (domain_id) REFERENCES domains(id) ON DELETE SET NULL,
    CONSTRAINT fk_company_type FOREIGN KEY (company_type_id) REFERENCES company_types(id) ON DELETE SET NULL
);

ALTER TABLE users
ADD COLUMN company_id BIGINT,
ADD CONSTRAINT fk_company_user FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL;



CREATE TABLE company_skills (
    company_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (company_id, skill_id),
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

