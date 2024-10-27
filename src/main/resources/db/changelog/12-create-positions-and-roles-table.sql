--CREATE TABLE position_roles (
--    id BIGSERIAL PRIMARY KEY,
--    name VARCHAR(255) NOT NULL UNIQUE
--);

CREATE TABLE rates (
    id BIGSERIAL PRIMARY KEY,
    min INTEGER,
    max INTEGER,
    currency VARCHAR(10)
);

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE positions (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE SET NULL,
--    role_id BIGINT REFERENCES position_roles(id) ON DELETE CASCADE,
    pattern_id BIGINT REFERENCES patterns(id) ON DELETE CASCADE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    seniority_id BIGINT REFERENCES seniorities(id) ON DELETE SET NULL,
    rate_id BIGINT REFERENCES rates(id) ON DELETE SET NULL,
    min_years_experience INTEGER CHECK (min_years_experience >= 0),
    location_id BIGINT REFERENCES locations(id) ON DELETE SET NULL,
    hours_per_week INTEGER CHECK (hours_per_week >= 0 AND hours_per_week <= 168),
    hiring_process VARCHAR(255),
    description TEXT
);

CREATE TABLE positions_optional_skills (
    position_id BIGINT REFERENCES positions(id),
    optional_skills_id BIGINT REFERENCES skills(id),
    PRIMARY KEY (position_id, optional_skills_id)
);

CREATE TABLE experience (
    id BIGSERIAL PRIMARY KEY,
    months INTEGER,
    years INTEGER
);


CREATE TABLE required_skill (
    id BIGSERIAL PRIMARY KEY,
    position_id BIGINT REFERENCES positions(id),
    skill_id BIGINT REFERENCES skills(id),
    experience_id BIGINT REFERENCES experience(id)
);

CREATE TABLE position_responsibilities (
    position_id BIGINT REFERENCES positions(id),
    responsibilities TEXT,
    PRIMARY KEY (position_id, responsibilities)
);

CREATE TABLE work_modes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE position_work_modes (
    position_id BIGINT REFERENCES positions(id) ON DELETE CASCADE,
    work_mode_id BIGINT REFERENCES work_modes(id),
    PRIMARY KEY (position_id, work_mode_id)
);

INSERT INTO work_modes (name) VALUES
('OFFICE'),
('HYBRID'),
('REMOTE');
