--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Add new column to sync contract configuration "start of HR processes after sync end"

ALTER TABLE sys_sync_contract_config ADD COLUMN start_hr_processes boolean;
UPDATE sys_sync_contract_config SET start_hr_processes = true WHERE start_hr_processes is null;
ALTER TABLE sys_sync_contract_config ALTER COLUMN start_hr_processes SET NOT NULL;

ALTER TABLE sys_sync_contract_config_a ADD COLUMN start_hr_processes boolean;
ALTER TABLE sys_sync_contract_config_a ADD COLUMN start_of_hr_processes_m boolean;


