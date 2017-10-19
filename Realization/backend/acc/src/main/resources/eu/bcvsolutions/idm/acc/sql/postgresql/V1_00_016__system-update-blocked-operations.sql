--
-- CzechIdM 7.4 Flyway script 
-- BCV solutions s.r.o.
--
-- Set default values for blocked operations on system,
-- also set default value for existing items.

UPDATE sys_system SET create_operation = false;
UPDATE sys_system SET update_operation = false;
UPDATE sys_system SET delete_operation = false;

ALTER TABLE sys_system ALTER COLUMN create_operation SET DEFAULT false;
ALTER TABLE sys_system ALTER COLUMN update_operation SET DEFAULT false;
ALTER TABLE sys_system ALTER COLUMN delete_operation SET DEFAULT false;
