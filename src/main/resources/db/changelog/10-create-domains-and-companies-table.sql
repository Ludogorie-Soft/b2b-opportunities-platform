CREATE TABLE domains (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO domains (name) VALUES
('Technology'),
('Fintech'),
('Automotive');

CREATE TABLE company_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO company_types (name) VALUES
('Product and service'),
('Product');

CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    website VARCHAR(255) NOT NULL,
    email_verification VARCHAR(255),
    domain_id BIGINT,
    linked_in VARCHAR(255),
    description TEXT,
    company_type_id BIGINT,
    CONSTRAINT fk_domain FOREIGN KEY (domain_id) REFERENCES domains(id) ON DELETE SET NULL,
    CONSTRAINT fk_company_type FOREIGN KEY (company_type_id) REFERENCES company_types(id) ON DELETE SET NULL
);

ALTER TABLE users
ADD COLUMN company_id BIGINT,
ADD CONSTRAINT fk_company_user FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL;

CREATE TABLE company_skills (
    company_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (company_id, skill_id),
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

INSERT INTO companies (name, email, website, email_verification, domain_id, linked_in, description, company_type_id) VALUES
('Microsoft', 'microsoft@email.com', 'https://www.microsoft.com/bg-bg/', 'ACCEPTED' , 1, 'https://www.linkedin.com/company/microsoft', '## About us\nEvery company has a mission. What''s ours? To empower every person and every organization to achieve more. We believe technology can and should be a force for good and that meaningful innovation contributes to a brighter world in the future and today. Our culture doesn''t just encourage curiosity; it embraces it. Each day we make progress together by showing up as our authentic selves. We show up with a learn-it-all mentality. We show up cheering on others, knowing their success doesn''t diminish our own. We show up every day open to learning our own biases, changing our behavior, and inviting in differences. Because impact matters.\n\nMicrosoft operates in 190 countries and is made up of more than 220,000 passionate employees worldwide.', 1),
('Meta', 'meta@email.com', 'https://about.meta.com/', 'ACCEPTED', 1, 'https://www.linkedin.com/company/meta', 'Meta builds technologies that help people connect, find communities, and grow businesses. When Facebook launched in 2004, it changed the way people connect. Apps like Messenger, Instagram and WhatsApp further empowered billions around the world. Now, Meta is moving beyond 2D screens toward immersive experiences like augmented and virtual reality to help build the next evolution in social technology. People who choose to build their careers by building with us at Meta help shape a future that will take us beyond what digital connection makes possible today—beyond the constraints of screens, the limits of distance, and even the rules of physics.', 1),
('myPOS Technologies', 'mypos@email.com', 'https://www.mypos.com/bg-bg', 'ACCEPTED', 2, 'https://bg.linkedin.com/company/mypos-official', 'myPOS is an international fintech company. Our team of 550+ people is located in 18 offices all over Europe. The company works in 30+ markets in the continent and has more than 150,000 business customers. myPOS offers a complete business solution for payments through POS terminals, online and via mobile phone. We provide integrated and affordable payment solutions, changing the way businesses accept card payments across all channels - at the counter, online and via mobile devices.\n\nOur mission is to empower every enterprise to reap the benefits of innovation and modern technologies, solve payment challenges and grow in new and powerful ways. By combining the latest payment technologies with imagination and expertise, we create a new world of payments, built on innovation, freedom, flexibility and opportunities for growth. Any business, no matter its size and type, can be a part of it.', 1),
('Bosch', 'bosch@email.com', 'https://www.bosch.com/', 'ACCEPTED', 3, 'https://www.linkedin.com/company/bosch', 'Bosch is a leading global supplier of technology and engineering services. Founded in 1886, the company has grown to become a global player in various industries, including automotive, industrial technology, consumer goods, and energy and building technology.', 2);

INSERT INTO company_skills (company_id, skill_id) VALUES
(1, 55),
(1, 100),
(1, 121),
(2, 116),
(2, 111),
(2, 92),
(3, 67),
(3, 75),
(3, 80);

