CREATE TABLE position_status (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO position_status (name) VALUES ('Opened');
INSERT INTO position_status (name) VALUES ('Filled from the platform');
INSERT INTO position_status (name) VALUES ('Filled outside of the platform');
INSERT INTO position_status (name) VALUES ('Canceled');

ALTER TABLE positions ADD COLUMN status_id BIGINT;

ALTER TABLE positions
ADD CONSTRAINT fk_status FOREIGN KEY (status_id) REFERENCES position_status(id);

ALTER TABLE positions
DROP COLUMN is_active;