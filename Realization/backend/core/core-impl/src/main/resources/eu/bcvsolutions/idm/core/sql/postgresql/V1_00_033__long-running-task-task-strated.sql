--
-- CzechIdM 7.2 Flyway script
-- BCV solutions s.r.o.
--
-- Add taskStarted DateTime to long running tasks
ALTER TABLE idm_long_running_task ADD COLUMN task_started timestamp without time zone;
