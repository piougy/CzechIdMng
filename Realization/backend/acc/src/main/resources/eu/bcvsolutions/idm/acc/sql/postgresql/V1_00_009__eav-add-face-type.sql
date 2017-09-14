--
-- CzechIdM 7.2 Flyway script 
-- BCV solutions s.r.o.
--
-- add new face type attribut and migrate persistent types

-- clean up persistent type 
update sys_system_form_value set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update sys_system_form_value set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
update sys_system_form_value_a set persistent_type = 'TEXT' where persistent_type = 'TEXTAREA' or persistent_type = 'RICHTEXTAREA';
update sys_system_form_value_a set persistent_type = 'DOUBLE' where persistent_type = 'CURRENCY';
--
ALTER TABLE sys_system_form_value ADD COLUMN uuid_value bytea;
ALTER TABLE sys_system_form_value_a ADD COLUMN uuid_value bytea;
ALTER TABLE sys_system_form_value_a ADD COLUMN uuid_value_m boolean;

