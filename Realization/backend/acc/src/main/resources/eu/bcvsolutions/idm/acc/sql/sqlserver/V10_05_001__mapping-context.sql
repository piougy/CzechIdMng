--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add mapping context

ALTER TABLE sys_system_mapping ADD add_context_contracts bit NOT NULL DEFAULT 0;
ALTER TABLE sys_system_mapping_a ADD add_context_contracts bit;
ALTER TABLE sys_system_mapping_a ADD add_context_contracts_m bit;

ALTER TABLE sys_system_mapping ADD add_context_roles_sys bit NOT NULL DEFAULT 0;
ALTER TABLE sys_system_mapping_a ADD add_context_roles_sys bit;
ALTER TABLE sys_system_mapping_a ADD add_context_identity_roles_for_system_m bit;

ALTER TABLE sys_system_mapping ADD add_context_identity_roles bit NOT NULL DEFAULT 0;
ALTER TABLE sys_system_mapping_a ADD add_context_identity_roles bit;
ALTER TABLE sys_system_mapping_a ADD add_context_identity_roles_m bit;

ALTER TABLE sys_system_mapping ADD add_context_con_obj bit NOT NULL DEFAULT 0;
ALTER TABLE sys_system_mapping_a ADD add_context_con_obj bit;
ALTER TABLE sys_system_mapping_a ADD add_context_connector_object_m bit;

ALTER TABLE sys_system_mapping ADD mapping_context_script nvarchar(MAX);
ALTER TABLE sys_system_mapping_a ADD mapping_context_script nvarchar(MAX);
ALTER TABLE sys_system_mapping_a ADD mapping_context_script_m bit;
