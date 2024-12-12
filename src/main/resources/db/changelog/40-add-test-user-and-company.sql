INSERT INTO companies (name, email, website, email_verification, domain_id, linked_in, description, company_type_id, email_confirmation_token, is_approved) VALUES
('Test Company', 'test@test.com', 'https://test.com/', 'ACCEPTED', 1, 'https://www.linkedin.com/test', 'A test company', 1, NULL, true);

INSERT INTO users (username, first_name, last_name, email, password, role_id, created_at, is_enabled, provider, company_id) VALUES
('test', 'firstName', 'lastName', 'test@test.com', '$2a$10$ldzyWc9QuFuPl95LdnEWZORF6SrxGpzI1AOsxNHVg9I6uVRXhmin6', 2, '2024-12-12 12:00:00.000', true, NULL, 5);