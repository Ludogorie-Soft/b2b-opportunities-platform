DELETE FROM confirmation_token;

DELETE FROM users
WHERE username != 'admin';

DELETE FROM talents;
DELETE FROM talent_work_modes;
DELETE FROM skill_experience;
DELETE FROM talent_experience;
DELETE FROM talent_locations;
DELETE FROM required_skill;
DELETE FROM position_responsibilities;
DELETE FROM positions;
DELETE FROM rates;
DELETE FROM projects;
DELETE FROM partner_groups;
DELETE FROM partner_group_companies;
DELETE FROM project_partner_group;
DELETE FROM positions_optional_skills;
DELETE FROM position_work_modes;
DELETE FROM position_applications;
DELETE FROM filters;
DELETE FROM filter_skills;
DELETE FROM company_talent_access_groups;
DELETE FROM company_skills;
DELETE FROM company_project_notified;
DELETE FROM company_partner_groups;

DELETE FROM companies
WHERE name != 'Test Company';