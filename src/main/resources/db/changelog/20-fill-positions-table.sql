INSERT INTO rates(id, min, max, currency) VALUES
(1, 20, 33, 'EUR'),
(2, 33, 53, 'EUR'),
(3, 20, NULL, 'EUR'),
(4, 40, 63, 'EUR'),
(5, 16, 20, 'EUR'),
(6, 26, 32, 'EUR'),
(7, 23, 30, 'EUR'),
(8, 42, 54, 'EUR');

INSERT INTO locations(name) VALUES
('Plovdiv'),
('Varna'),
('Sofia');

INSERT INTO experience(months, years) VALUES
(5,4),
(null,6),
(null,4),
(null,2),
(4,6),
(null,5),
(null,3),
(null,1);

INSERT INTO positions(project_id, pattern_id, seniority_id, rate_id, min_years_experience, location_id, hours_per_week, status_id, hiring_process, description) VALUES
(1, 9, 4, 1, 2, 1, 40, 1, '1. Coding task\n2. Technical interview', '## Overview\n\nThe Web Experiences Team, WebXT, builds comprehensive and engaging content, services, and platforms for consumers to access the information they need anywhere on any device and for enterprises to enhance their employee and customer experiences. The engineers on our front-end team build the web applications that provide full self-service access for our customers to our e-commerce promotions platform. We are looking for a front-end engineer with a demonstrated track record of innovative thinking, and technical excellence.\n\nThe Search + Distribution organization includes the product, engineering, and growth teams responsible for Microsoft Bing worldwide, as well as Microsoft Search in Bing for enterprise. Our mission is to delight users everywhere with the best search experience. We are focused on creating competitive and differentiated search quality experiences, which we do by applying highly advanced ML technologies such as large-scale deep learning models and by investing in more modern search experiences.\n\nOur Culture: Microsoft Culture Our Team: Inside Microsoft''s Web Experiences Team\n\n'),
(1, 3, 3, 2, 3, NULL, 40, 1, '1. Coding task\n2. Technical interview', '## This is a job advert\nThis is the advert''s description. It **supports** *mark*_down_\n- This\n- is\n- a\n- list'),
(1, 29, 3, 3, NULL, NULL, 40, 1, '1. Coding task\n2. Technical interview', '## This is a job advert\nThis is the advert''s description. It **supports** *mark*_down_\n- This\n- is\n- a\n- list'),
(2, 15, 4, 4, NULL, NULL, 40, 1, NULL, '## About the job\n\nThe ideal candidate will have industry experience working on a range of recommendation, classification, and optimization problems. You will bring the ability to own the whole ML life cycle, define projects and drive excellence across teams. You will work alongside the world’s leading engineers and researchers to solve some of the most exciting and massive social data and prediction problems that exist on the web.\n\n'),
(3, 9, 2, 5, 2, NULL, 40, 1, NULL, 'We are seeking a talented React.js Developer to join our team and contribute to the development of innovative web applications. As a React.js Developer, you will be responsible for designing, developing, and maintaining complex React.js applications, collaborating with designers and back-end developers to deliver high-quality products, writing clean and efficient code, optimizing application performance, and staying up-to-date with the latest React.js trends. Ideal candidates will have strong proficiency in JavaScript and React.js, experience with popular React libraries and tools, understanding of modern web development concepts, ability to work independently and as part of a team, excellent problem-solving skills, and a passion for creating exceptional user experiences. Bonus points for experience with TypeScript, knowledge of testing frameworks, and contributions to open-source React projects. If you are a talented React.js developer excited to join a fast-paced and innovative company, we encourage you to apply.'),
(3, 9, 4, 6, 3, 2, 40, 1, NULL, 'We are seeking a talented React.js Developer to join our team and contribute to the development of innovative web applications. As a React.js Developer, you will be responsible for designing, developing, and maintaining complex React.js applications, collaborating with designers and back-end developers to deliver high-quality products, writing clean and efficient code, optimizing application performance, and staying up-to-date with the latest React.js trends. Ideal candidates will have strong proficiency in JavaScript and React.js, experience with popular React libraries and tools, understanding of modern web development concepts, ability to work independently and as part of a team, excellent problem-solving skills, and a passion for creating exceptional user experiences. Bonus points for experience with TypeScript, knowledge of testing frameworks, and contributions to open-source React projects. If you are a talented React.js developer excited to join a fast-paced and innovative company, we encourage you to apply.'),
(4, 30, 3, 7, 4, NULL, 40, 1, NULL, '## Role Summary\n\n- Expert role in system engineering for development of automotive solutions from prototype phase through production launch.\n- Mastering all aspects of system engineering including functional analyzes, system architecture, system requirements definition. DFMEA, DFA and etc\n- Work together with customer system team on a product’s system development concept'),
(4, 24, 4, 8, 6, 3, 30, 1, NULL, 'As a Project Manager, you will play a crucial role in driving successful project outcomes. You will be responsible for developing and implementing comprehensive project plans, leading project teams, monitoring project progress, managing budgets and resources, facilitating effective communication, ensuring quality standards, and providing regular project updates. To be successful in this role, you will need proven experience in project management, strong project management methodologies, excellent organizational and communication skills, and proficiency in project management tools. Preferred qualifications include experience in [industry or domain relevant to the company], PMP certification, and experience leading cross-functional teams. If you are a passionate and results-oriented Project Manager looking for a challenging and rewarding opportunity, we encourage you to apply.');

INSERT INTO position_responsibilities(position_id, responsibilities) VALUES
(1,'Identifying technical problems'),
(1,'Designing computer-based systems or programs'),
(2,'Identifying technical problems'),
(2,'Designing computer-based systems or programs'),
(3,'Working in an agile project team'),
(3,'Learning new hardware and software skills'),
(4,'Designing computer-based systems or programs'),
(4,'Oversee project direction'),
(4,'Research and evaluate emerging technologies, industry and market trends'),
(5,'Learning new hardware and software skills'),
(5,'Working in an agile project team'),
(6,'Research and evaluate emerging technologies, industry and market trends'),
(6,'Identifying technical problems'),
(6,'Working in an agile project team'),
(7,'Research and evaluate emerging technologies, industry and market trends');

INSERT INTO position_work_modes(position_id, work_mode_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(3, 3),
(4, 2),
(4, 3),
(5, 3),
(6, 1),
(7, 3),
(8, 1);

INSERT INTO required_skill(position_id, skill_id, experience_id) VALUES
(1, 85, 1),
(2, 56, NULL),
(2, 63, NULL),
(3, 131, NULL),
(4, 61, 2),
(4, 42, 3),
(5, 50, 4),
(5, 85, 4),
(6, 85, 5),
(6, 21, 6),
(7, 17, 7),
(7, 25, 8),
(8, 106, 2),
(8, 104, 3),
(8, 105, 2);