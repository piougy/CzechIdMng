--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- New feature - Skip merge values if contract is excluded

ALTER TABLE sys_role_system_attribute ADD skip_value_if_excluded bit NOT NULL DEFAULT 0;
ALTER TABLE sys_role_system_attribute_a ADD skip_value_if_excluded bit;
ALTER TABLE sys_role_system_attribute_a ADD skip_value_if_excluded_m bit;
