--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Remove columns keept wf definitions on role and add new boolean column for approve remove WF

ALTER TABLE idm_role DROP COLUMN approve_remove_workflow;
ALTER TABLE idm_role DROP COLUMN approve_add_workflow;
ALTER TABLE idm_role ADD COLUMN approve_remove boolean NOT NULL DEFAULT false;

ALTER TABLE idm_role_a DROP COLUMN approve_remove_workflow;
ALTER TABLE idm_role_a DROP COLUMN approve_add_workflow;
ALTER TABLE idm_role_a ADD COLUMN approve_remove boolean NOT NULL DEFAULT false;

ALTER TABLE idm_role_a DROP COLUMN approve_remove_workflow_m;
ALTER TABLE idm_role_a DROP COLUMN approve_add_workflow_m;
ALTER TABLE idm_role_a ADD COLUMN approve_remove_m boolean;