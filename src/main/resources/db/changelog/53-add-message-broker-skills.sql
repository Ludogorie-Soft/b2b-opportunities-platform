UPDATE skills
SET assignable = true
WHERE id = 12;

INSERT INTO skills (id, name, parent_id, assignable, image, image_type) VALUES
(159, 'Message Broker', 2, false, NULL, NULL),
(160, 'Apache Kafka', 159, true, NULL, NULL),
(161, 'RabbitMQ', 159, true, NULL, NULL),
(162, 'ActiveMQ', 159, true, NULL, NULL);