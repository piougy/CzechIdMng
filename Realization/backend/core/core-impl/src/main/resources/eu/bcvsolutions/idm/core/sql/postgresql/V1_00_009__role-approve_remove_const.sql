--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Remove constaint form audit table

ALTER TABLE idm_role_a ALTER COLUMN approve_remove DROP NOT NULL;