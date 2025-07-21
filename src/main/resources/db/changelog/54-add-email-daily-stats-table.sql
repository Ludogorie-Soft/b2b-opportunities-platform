CREATE TABLE email_daily_stats (
    id BIGSERIAL PRIMARY KEY,
    day DATE NOT NULL UNIQUE,
    activation_mails_sent INTEGER NOT NULL DEFAULT 0,
    activation_mails_opened INTEGER NOT NULL DEFAULT 0,
    new_project_mails_sent INTEGER NOT NULL DEFAULT 0,
    new_application_mails_sent INTEGER NOT NULL DEFAULT 0
);
