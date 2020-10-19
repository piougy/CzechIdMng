--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Support for dynamic Vector in confidential storage

ALTER TABLE idm_confidential_storage ADD COLUMN iv bytea NULL;
