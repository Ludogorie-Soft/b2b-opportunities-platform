ALTER TABLE projects
ADD COLUMN is_partner_only BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE partner_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE company_partner_groups (
    company_id BIGINT NOT NULL,
    partner_group_id BIGINT NOT NULL,
    PRIMARY KEY (company_id, partner_group_id),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (partner_group_id) REFERENCES partner_groups(id) ON DELETE CASCADE
);

--CREATE TABLE partner_group_companies (
--    partner_group_id BIGINT NOT NULL,
--    company_id BIGINT NOT NULL,
--    PRIMARY KEY (partner_group_id, company_id),
--    FOREIGN KEY (partner_group_id) REFERENCES partner_groups(id),
--    FOREIGN KEY (company_id) REFERENCES companies(id)
--);