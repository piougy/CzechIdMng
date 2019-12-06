--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- New feature - Skip merge values if contract is excluded

ALTER TABLE sys_role_system_attribute ADD COLUMN skip_value_if_excluded bool NOT NULL DEFAULT false;
ALTER TABLE sys_role_system_attribute_a ADD COLUMN skip_value_if_excluded bool;
ALTER TABLE sys_role_system_attribute_a ADD COLUMN skip_value_if_excluded_m bool;
