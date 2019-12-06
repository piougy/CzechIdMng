--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Sync option: Assign default role to all valid or future valid contracts

ALTER TABLE sys_sync_identity_config ADD COLUMN assign_default_role_to_all bool NOT NULL DEFAULT false;
ALTER TABLE sys_sync_identity_config_a ADD COLUMN assign_default_role_to_all bool;
ALTER TABLE sys_sync_identity_config_a ADD COLUMN assign_default_role_to_all_m bool;
