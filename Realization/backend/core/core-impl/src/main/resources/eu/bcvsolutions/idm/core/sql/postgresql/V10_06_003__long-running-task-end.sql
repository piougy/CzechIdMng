--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- task end date

ALTER TABLE idm_long_running_task ADD COLUMN task_ended timestamp without time zone;
ALTER TABLE idm_long_running_task_a ADD COLUMN task_ended timestamp without time zone;
ALTER TABLE idm_long_running_task_a ADD COLUMN task_ended_m bool;

UPDATE idm_long_running_task SET task_ended = modified WHERE result_state <> 'CREATED' AND result_state <> 'RUNNING';

