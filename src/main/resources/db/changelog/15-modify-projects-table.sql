ALTER TABLE projects
ADD COLUMN project_status VARCHAR(255),
ADD COLUMN token VARCHAR(255);

--Set active status for the 4 projects that we have in the migrations
UPDATE projects
SET project_status = 'ACTIVE';