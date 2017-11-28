ALTER TABLE idm_password
DROP COLUMN unsuccessful_attemps;

ALTER TABLE idm_password ADD COLUMN unsuccessful_attemps
SMALLINT NOT NULL
DEFAULT 0;