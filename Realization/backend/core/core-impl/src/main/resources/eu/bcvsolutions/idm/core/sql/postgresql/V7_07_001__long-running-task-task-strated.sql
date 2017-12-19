--
-- CzechIdM 7.7 Flyway script
-- BCV solutions s.r.o.
--
-- Add taskStarted DateTime to long running tasks
ALTER TABLE idm_long_running_task ADD COLUMN task_started timestamp without time zone;

UPDATE idm_long_running_task SET task_started = created WHERE result_state = 'CREATED';
