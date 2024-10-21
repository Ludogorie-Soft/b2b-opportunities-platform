CREATE TABLE company_project_notified (
    company_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    PRIMARY KEY (company_id, project_id),
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

ALTER TABLE companies ADD COLUMN project_ids_notified BIGINT[] DEFAULT '{}';

ALTER TABLE projects ADD COLUMN date_updated TIMESTAMP;

UPDATE projects
SET date_updated = date_posted;