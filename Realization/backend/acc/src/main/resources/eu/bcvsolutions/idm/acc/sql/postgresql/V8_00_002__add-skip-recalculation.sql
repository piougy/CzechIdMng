--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Add start recalculation for identity and identity contract synchronization config.

ALTER TABLE sys_sync_contract_config ADD COLUMN start_auto_role_rec boolean;
UPDATE sys_sync_contract_config SET start_auto_role_rec = true WHERE start_auto_role_rec is null;
ALTER TABLE sys_sync_contract_config ALTER COLUMN start_auto_role_rec SET NOT NULL;

ALTER TABLE sys_sync_contract_config_a ADD COLUMN start_auto_role_rec boolean;
ALTER TABLE sys_sync_contract_config_a ADD COLUMN start_auto_role_recalculation_m boolean;


ALTER TABLE sys_sync_identity_config ADD COLUMN start_auto_role_rec boolean;
UPDATE sys_sync_identity_config SET start_auto_role_rec = true WHERE start_auto_role_rec is null;
ALTER TABLE sys_sync_identity_config ALTER COLUMN start_auto_role_rec SET NOT NULL;

ALTER TABLE sys_sync_identity_config_a ADD COLUMN start_auto_role_rec boolean;
ALTER TABLE sys_sync_identity_config_a ADD COLUMN start_auto_role_recalculation_m boolean;
