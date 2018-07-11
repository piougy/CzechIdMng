--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Add new column for creating default contract during identities synchronization

ALTER TABLE sys_sync_identity_config ADD COLUMN create_default_contract boolean;
UPDATE sys_sync_identity_config SET create_default_contract = false WHERE create_default_contract is null;
ALTER TABLE sys_sync_identity_config ALTER COLUMN create_default_contract SET NOT NULL;

ALTER TABLE sys_sync_identity_config_a ADD COLUMN create_default_contract boolean;
ALTER TABLE sys_sync_identity_config_a ADD COLUMN create_default_contract_m boolean;
