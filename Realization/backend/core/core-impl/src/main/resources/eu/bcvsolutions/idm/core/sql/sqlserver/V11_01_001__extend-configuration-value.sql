--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- extend configuration value length

ALTER TABLE idm_configuration ALTER COLUMN value nvarchar(2000) NULL;
ALTER TABLE idm_configuration_a ALTER COLUMN value nvarchar(2000) NULL;

