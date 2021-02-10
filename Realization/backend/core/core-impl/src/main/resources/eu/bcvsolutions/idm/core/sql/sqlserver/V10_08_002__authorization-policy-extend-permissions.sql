--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- authorization policy - extend base permission size

ALTER TABLE idm_authorization_policy ALTER COLUMN base_permissions nvarchar(2000) NULL;
ALTER TABLE idm_authorization_policy_a ALTER COLUMN base_permissions nvarchar(2000) NULL;