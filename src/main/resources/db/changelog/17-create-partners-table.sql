CREATE TABLE partner_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE partner_group_companies (
    partner_group_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    PRIMARY KEY (partner_group_id, company_id),

    CONSTRAINT fk_partner_group
        FOREIGN KEY (partner_group_id)
        REFERENCES partner_groups (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
        ON DELETE CASCADE
);

ALTER TABLE projects
ADD COLUMN is_partner_only BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE project_partner_group (
    project_id BIGINT NOT NULL,
    partner_group_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, partner_group_id),

    CONSTRAINT fk_project
        FOREIGN KEY (project_id)
        REFERENCES projects (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_partner_group
        FOREIGN KEY (partner_group_id)
        REFERENCES partner_groups (id)
        ON DELETE CASCADE
);