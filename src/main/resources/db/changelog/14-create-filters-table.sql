CREATE TABLE filters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    company_id BIGINT,
    is_enabled BOOLEAN NOT NULL,
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE TABLE filter_skills (
    filter_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (filter_id, skill_id),
    CONSTRAINT fk_company FOREIGN KEY (filter_id) REFERENCES filters(id) ON DELETE CASCADE,
    CONSTRAINT fk_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);
