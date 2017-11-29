--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Add new column to system mapping with script 'Can be account created?'"

ALTER TABLE sys_system_mapping ADD COLUMN can_be_acc_created_script text;
ALTER TABLE sys_system_mapping_a ADD COLUMN can_be_acc_created_script text;
ALTER TABLE sys_system_mapping_a ADD COLUMN can_be_account_created_script_m boolean;


