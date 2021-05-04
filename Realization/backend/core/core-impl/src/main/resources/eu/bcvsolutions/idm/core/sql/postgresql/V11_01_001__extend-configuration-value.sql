--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- extend configuration value length

ALTER TABLE idm_configuration ALTER COLUMN value TYPE varchar(2000);
ALTER TABLE idm_configuration_a ALTER COLUMN value TYPE varchar(2000);

