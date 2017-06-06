--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Add stateful flag to long running tasks
ALTER TABLE idm_long_running_task ADD COLUMN stateful boolean NOT NULL DEFAULT TRUE;