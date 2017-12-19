--
-- CzechIdM 7.7 Flyway script
-- BCV solutions s.r.o.
--
-- Add dryRun boolean to long running tasks
ALTER TABLE idm_long_running_task ADD COLUMN dry_run boolean NOT NULL DEFAULT false;
ALTER TABLE idm_scheduled_task DROP COLUMN dry_run;
ALTER TABLE idm_scheduled_task_a DROP COLUMN dry_run;
