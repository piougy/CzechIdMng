--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- extend role request description value length

ALTER TABLE idm_role_request ALTER COLUMN description nvarchar(2000) NULL;
ALTER TABLE idm_role_request_a ALTER COLUMN description nvarchar(2000) NULL;

