ALTER TABLE project_partner_group
DROP CONSTRAINT fk_partner_group;

ALTER TABLE project_partner_group
ADD CONSTRAINT fk_partner_group
FOREIGN KEY (partner_group_id) REFERENCES partner_groups(id) ON DELETE CASCADE;