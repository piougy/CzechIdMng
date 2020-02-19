--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add recoveable flag to long running tasks - task cabe be executed again.

ALTER TABLE idm_long_running_task ADD COLUMN recoverable boolean NOT NULL DEFAULT false;