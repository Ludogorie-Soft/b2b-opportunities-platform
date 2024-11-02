CREATE TABLE talents (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT REFERENCES companies(id),
    description TEXT,
    is_active BOOLEAN NOT NULL,
    residence VARCHAR(50)
);

CREATE TABLE talent_experience (
    id BIGSERIAL PRIMARY KEY,
    total_time INT NOT NULL,
    pattern_id BIGINT REFERENCES patterns(id),
    seniority_id BIGINT REFERENCES seniorities(id),
    talent_id BIGINT REFERENCES talents(id)
);

CREATE TABLE skill_experience (
    id BIGSERIAL PRIMARY KEY,
    talent_experience_id BIGINT REFERENCES talent_experience(id),
    skill_id BIGINT REFERENCES skills(id),
    experience_id BIGINT REFERENCES experience(id)
);