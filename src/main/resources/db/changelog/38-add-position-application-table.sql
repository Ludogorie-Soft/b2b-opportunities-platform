CREATE TABLE position_applications (
    id BIGSERIAL PRIMARY KEY,
    position_id BIGINT NOT NULL,
    talent_id BIGINT NOT NULL,
    application_status VARCHAR(255) NOT NULL,
    rate INT NOT NULL,
    application_date_time TIMESTAMP NOT NULL,
    available_from TIMESTAMP NOT NULL,
    FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE CASCADE,
    FOREIGN KEY (talent_id) REFERENCES talents(id) ON DELETE CASCADE
);