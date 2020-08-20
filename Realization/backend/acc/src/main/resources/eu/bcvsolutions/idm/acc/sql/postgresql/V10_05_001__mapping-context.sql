--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add mapping context

ALTER TABLE sys_system_mapping ADD COLUMN add_context_contracts boolean NOT NULL DEFAULT False;
ALTER TABLE sys_system_mapping_a ADD COLUMN add_context_contracts boolean;
ALTER TABLE sys_system_mapping_a ADD COLUMN add_context_contracts_m boolean;

ALTER TABLE sys_system_mapping ADD COLUMN add_context_roles_sys boolean NOT NULL DEFAULT False;
ALTER TABLE sys_system_mapping_a ADD COLUMN add_context_roles_sys boolean;
ALTER TABLE sys_system_mapping_a ADD COLUMN add_context_identity_roles_for_system_m boolean;

ALTER TABLE sys_system_mapping ADD COLUMN add_context_identity_roles boolean NOT NULL DEFAULT False;
ALTER TABLE sys_system_mapping_a ADD COLUMN add_context_identity_roles boolean;
ALTER TABLE sys_system_mapping_a ADD COLUMN add_context_identity_roles_m boolean;

ALTER TABLE sys_system_mapping ADD COLUMN add_context_con_obj boolean NOT NULL DEFAULT False;
ALTER TABLE sys_system_mapping_a ADD COLUMN add_context_con_obj boolean;
ALTER TABLE sys_system_mapping_a ADD COLUMN add_context_connector_object_m boolean;

ALTER TABLE sys_system_mapping ADD COLUMN mapping_context_script text;
ALTER TABLE sys_system_mapping_a ADD COLUMN mapping_context_script text;
ALTER TABLE sys_system_mapping_a ADD COLUMN mapping_context_script_m boolean;
