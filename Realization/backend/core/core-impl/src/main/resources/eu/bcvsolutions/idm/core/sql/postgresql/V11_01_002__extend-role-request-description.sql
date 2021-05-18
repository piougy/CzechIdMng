--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- extend role request description value length

ALTER TABLE idm_role_request ALTER COLUMN description TYPE varchar(2000);
ALTER TABLE idm_role_request_a ALTER COLUMN description TYPE varchar(2000);

