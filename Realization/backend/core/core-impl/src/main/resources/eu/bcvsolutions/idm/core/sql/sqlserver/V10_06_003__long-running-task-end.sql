--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- task end date

ALTER TABLE idm_long_running_task ADD task_ended datetime2(6);
ALTER TABLE idm_long_running_task_a ADD task_ended datetime2(6);
ALTER TABLE idm_long_running_task_a ADD task_ended_m bit;

UPDATE idm_long_running_task SET task_ended = modified WHERE result_state <> 'CREATED' AND result_state <> 'RUNNING';

