--
-- CzechIdM 7.6 Flyway script 
-- BCV solutions s.r.o.
--
-- identity lastname can be null

ALTER TABLE idm_identity ALTER COLUMN last_name DROP NOT NULL;


