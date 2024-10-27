ALTER TABLE projects
ADD COLUMN is_partner_only BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE partner_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    company_id BIGINT NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT unique_company_name UNIQUE (company_id, name)
);

CREATE TABLE company_partner_groups (
    company_id BIGINT NOT NULL,
    partner_group_id BIGINT NOT NULL,
    PRIMARY KEY (company_id, partner_group_id),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (partner_group_id) REFERENCES partner_groups(id) ON DELETE CASCADE
);

CREATE TABLE partner_group_companies (
    partner_group_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    PRIMARY KEY (partner_group_id, company_id),
    FOREIGN KEY (partner_group_id) REFERENCES partner_groups(id),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE project_partner_group (
    project_id BIGINT NOT NULL,
    partner_group_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, partner_group_id),
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_partner_group FOREIGN KEY (partner_group_id) REFERENCES partner_groups(id)
);