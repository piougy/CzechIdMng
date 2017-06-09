--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
-- Add attributes for account protection mode

ALTER TABLE sys_system_mapping_a ADD COLUMN protection_interval_m boolean;
ALTER TABLE sys_system_mapping_a ADD COLUMN protection_interval integer;
ALTER TABLE sys_system_mapping_a ADD COLUMN protection_enabled_m boolean;
ALTER TABLE sys_system_mapping_a ADD COLUMN protection_enabled boolean;
ALTER TABLE sys_system_mapping ADD COLUMN protection_enabled boolean;
ALTER TABLE sys_system_mapping ADD COLUMN protection_interval integer;
ALTER TABLE acc_account_a ADD COLUMN end_of_protection timestamp without time zone;
ALTER TABLE acc_account_a ADD COLUMN end_of_protection_m boolean;
ALTER TABLE acc_account_a ADD COLUMN in_protection_m boolean;
ALTER TABLE acc_account_a ADD COLUMN in_protection boolean;
ALTER TABLE acc_account ADD COLUMN in_protection boolean;
ALTER TABLE acc_account ADD COLUMN end_of_protection timestamp without time zone;

UPDATE sys_system_mapping SET protection_enabled = false  where protection_enabled is null;
UPDATE sys_system_mapping SET protection_interval  = 1  where protection_interval is null;
UPDATE acc_account SET in_protection = false  where in_protection is null;
ALTER TABLE sys_system_mapping ADD CONSTRAINT sys_system_mapping_protection_interval_check
 CHECK (protection_interval >= 0);

