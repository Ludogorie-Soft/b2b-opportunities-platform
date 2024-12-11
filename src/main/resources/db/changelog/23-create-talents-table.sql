CREATE TABLE talents (
    id BIGSERIAL PRIMARY KEY,
    is_active BOOLEAN NOT NULL,
    company_id BIGINT,
    description TEXT,
    talent_experience_id BIGINT,
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE TABLE talent_work_modes (
    talent_id BIGINT NOT NULL,
    work_mode_id BIGINT NOT NULL,
    PRIMARY KEY (talent_id, work_mode_id),
    CONSTRAINT fk_talent FOREIGN KEY (talent_id) REFERENCES talents(id) ON DELETE CASCADE,
    CONSTRAINT fk_work_mode FOREIGN KEY (work_mode_id) REFERENCES work_modes(id) ON DELETE CASCADE
);

CREATE TABLE talent_locations (
    talent_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    PRIMARY KEY (talent_id, location_id),
    CONSTRAINT fk_talent_location FOREIGN KEY (talent_id) REFERENCES talents(id) ON DELETE CASCADE,
    CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
);

CREATE TABLE talent_experience (
    id BIGSERIAL PRIMARY KEY,
    talent_id BIGINT,
    pattern_id BIGINT,
    seniority_id BIGINT,
    total_time INT,
    CONSTRAINT fk_talent_experience_talent FOREIGN KEY (talent_id) REFERENCES talents(id) ON DELETE CASCADE,
    CONSTRAINT fk_pattern FOREIGN KEY (pattern_id) REFERENCES patterns(id) ON DELETE CASCADE,
    CONSTRAINT fk_seniority FOREIGN KEY (seniority_id) REFERENCES seniorities(id) ON DELETE SET NULL
);

ALTER TABLE talents
ADD CONSTRAINT fk_talent_experience FOREIGN KEY (talent_experience_id) REFERENCES talent_experience(id) ON DELETE CASCADE;

CREATE TABLE skill_experience (
    id BIGSERIAL PRIMARY KEY,
    talent_experience_id BIGINT NOT NULL,
    skill_id BIGINT,
    experience INT,
    CONSTRAINT fk_skill_experience FOREIGN KEY (talent_experience_id) REFERENCES talent_experience(id) ON DELETE CASCADE,
    CONSTRAINT fk_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

ALTER TABLE companies
ADD COLUMN talents_shared_publicly BOOLEAN DEFAULT TRUE;

CREATE TABLE company_talent_access_groups (
    company_id BIGINT NOT NULL,
    partner_group_id BIGINT NOT NULL,
    PRIMARY KEY (company_id, partner_group_id),
    CONSTRAINT fk_company_talent_access FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_partner_group_access FOREIGN KEY (partner_group_id) REFERENCES partner_groups(id) ON DELETE CASCADE
);