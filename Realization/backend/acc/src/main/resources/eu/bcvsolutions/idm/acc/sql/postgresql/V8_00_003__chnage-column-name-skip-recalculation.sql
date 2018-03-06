--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Rename coloumns skip automatic role recalculation for audit table.

ALTER TABLE sys_sync_contract_config_a RENAME COLUMN start_auto_role_recalculation_m TO start_auto_role_rec_m;
ALTER TABLE sys_sync_identity_config_a RENAME COLUMN start_auto_role_recalculation_m TO start_auto_role_rec_m;
