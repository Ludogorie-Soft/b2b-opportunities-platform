CREATE TABLE company_partners (
    company_id BIGINT NOT NULL,
    partner_id BIGINT NOT NULL,
    PRIMARY KEY (company_id, partner_id),

    CONSTRAINT fk_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_partner
        FOREIGN KEY (partner_id)
        REFERENCES companies (id)
        ON DELETE CASCADE
);

ALTER TABLE projects
ADD COLUMN is_partner_only BOOLEAN NOT NULL DEFAULT false;