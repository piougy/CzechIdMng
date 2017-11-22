
ALTER TABLE idm_password ADD COLUMN last_success_login DATETIME();
ALTER TABLE idm_password ADD COLUMN unsuccessful_attemps INT;