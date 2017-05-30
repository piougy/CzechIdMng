--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Fix role requestable audit columns - not null is not needed
ALTER TABLE idm_role_a ALTER COLUMN can_be_requested DROP NOT NULL;
ALTER TABLE idm_role_a ALTER COLUMN can_be_requested_m DROP NOT NULL;