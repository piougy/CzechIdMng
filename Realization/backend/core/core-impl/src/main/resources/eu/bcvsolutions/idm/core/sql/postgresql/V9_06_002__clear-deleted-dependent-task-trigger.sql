--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- clear dependent task trigerr skeletons

DELETE FROM idm_dependent_task_trigger WHERE initiator_task_id NOT IN (SELECT job_name FROM qrtz_job_details);
DELETE FROM idm_dependent_task_trigger WHERE dependent_task_id NOT IN (SELECT job_name FROM qrtz_job_details);
