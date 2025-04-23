DELETE FROM confirmation_token;

DELETE FROM position_applications;

DELETE FROM filter_skills;

DELETE FROM company_talent_access_groups;

DELETE FROM company_skills;

DELETE FROM company_project_notified;

DELETE FROM company_partner_groups;

DELETE FROM positions_optional_skills;

DELETE FROM position_responsibilities;

DELETE FROM position_work_modes;

DELETE FROM required_skill;

DELETE FROM skill_experience;

DELETE FROM talent_work_modes;

DELETE FROM talent_locations;

DELETE FROM partner_group_companies;
DELETE FROM project_partner_group;
DELETE FROM positions;
DELETE FROM projects;
DELETE FROM partner_groups;
DELETE FROM filters;

DELETE FROM companies
WHERE name != 'Test Company';

DELETE FROM users
WHERE username != 'admin';