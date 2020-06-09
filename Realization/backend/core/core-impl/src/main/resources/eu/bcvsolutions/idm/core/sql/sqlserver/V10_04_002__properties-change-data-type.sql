--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Enlarge properties for propjection and long running tasks.

ALTER TABLE idm_form_projection ALTER COLUMN projection_properties image NULL;
ALTER TABLE idm_form_projection_a ALTER COLUMN projection_properties image NULL;

ALTER TABLE idm_long_running_task ALTER COLUMN result_model image NULL;
ALTER TABLE idm_long_running_task_a ALTER COLUMN result_model image NULL;