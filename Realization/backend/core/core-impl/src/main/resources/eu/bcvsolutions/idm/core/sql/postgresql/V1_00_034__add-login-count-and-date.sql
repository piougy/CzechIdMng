
ALTER TABLE idm_password ADD COLUMN last_success_login timestamp without time zone;
ALTER TABLE idm_password ADD COLUMN unsuccessful_attemps INT;