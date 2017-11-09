--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- Additional attributes for password provisioning

ALTER TABLE sys_system_attribute_mapping ADD COLUMN send_on_password_change boolean NOT NULL DEFAULT false;;
ALTER TABLE sys_system_attribute_mapping_a ADD COLUMN send_on_password_change boolean;
ALTER TABLE sys_system_attribute_mapping_a ADD COLUMN send_on_password_change_m boolean;