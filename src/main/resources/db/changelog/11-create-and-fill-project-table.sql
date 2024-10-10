CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    date_posted TIMESTAMP,
    name VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    duration INT,
    description TEXT,
    CONSTRAINT fk_company_projects FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);


INSERT INTO projects (id, company_id, date_posted, name, start_date, end_date, duration, description) VALUES
(1, 1, '2024-08-22 07:39:50', 'Some front-end project', '2023-12-03', '2024-10-20', NULL,
    'This project seeks to assemble a skilled and dedicated team of front-end software developers to contribute to the development and maintenance of innovative web-based applications. The team will be responsible for designing, building, and testing user interfaces that are both visually appealing and highly functional.'),
(5, 1, '2024-08-22 07:39:50', 'Some back-end project', NULL, NULL, 8,
    'This project seeks to assemble a skilled and dedicated team of front-end software developers to contribute to the development and maintenance of innovative web-based applications. The team will be responsible for designing, building, and testing user interfaces that are both visually appealing and highly functional.'),
(2, 2, '2024-08-22 07:39:50', 'Machine Learning Project', '2024-10-20', '2024-10-20', NULL,
    'Meta is embarking on the most transformative change to its business and technology in company history, and our Machine Learning Engineers are at the forefront of this evolution. By leading crucial projects and initiatives that have never been done before, you have an opportunity to help us advance the way people connect around the world.\nThe ideal candidate will have industry experience working on a range of recommendation, classification, and optimization problems. You will bring the ability to own the whole ML life cycle, define projects and drive excellence across teams. You will work alongside the world’s leading engineers and researchers to solve some of the most exciting and massive social data and prediction problems that exist on the web.'),
(3, 3, '2024-02-01 00:00:00', 'New website', NULL, NULL, NULL,
    'We are seeking a talented web developer to create a modern, user-friendly website that accurately represents our brand and effectively communicates our services to our target audience. The successful candidate will be responsible for designing, developing, and launching a new website that meets our specific needs and goals.'),
(4, 4, '2024-02-01 00:00:00', 'System Engineering Department', NULL, NULL, NULL,
    'To establish a robust and efficient System Engineering Department capable of delivering high-quality system solutions that meet the evolving needs of the organization. This department will play a crucial role in ensuring the successful design, development, implementation, and maintenance of complex systems.');

SELECT setval('projects_id_seq', (SELECT MAX(id) FROM projects));