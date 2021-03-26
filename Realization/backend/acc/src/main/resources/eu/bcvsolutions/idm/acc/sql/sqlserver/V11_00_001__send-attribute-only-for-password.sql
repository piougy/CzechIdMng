--
-- CzechIdM 11.0.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Additional attributes for password provisioning

ALTER TABLE sys_system_attribute_mapping ADD send_only_on_password_change bit NOT NULL DEFAULT 0;
ALTER TABLE sys_system_attribute_mapping_a ADD send_only_on_password_change bit;
ALTER TABLE sys_system_attribute_mapping_a ADD send_only_on_password_change_m bit;
