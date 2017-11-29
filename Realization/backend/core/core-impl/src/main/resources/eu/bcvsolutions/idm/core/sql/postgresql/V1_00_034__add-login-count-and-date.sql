
ALTER TABLE idm_password ADD COLUMN last_successful_login timestamp without time zone;
ALTER TABLE idm_password ADD COLUMN unsuccessful_attempts SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE idm_password_a ADD COLUMN last_successful_login timestamp without time zone;
ALTER TABLE idm_password_a ADD COLUMN last_successful_login_m boolean;

ALTER TABLE idm_password_a ADD COLUMN unsuccessful_attempts SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE idm_password_a ADD COLUMN unsuccessful_attempts_m boolean;
