--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Add new column to attribute mapping 'Is attribute cached?'"

ALTER TABLE sys_system_attribute_mapping ADD COLUMN attribute_cached boolean;
-- All exists attributes will be not cached (for back compatibility)
UPDATE sys_system_attribute_mapping SET attribute_cached = false;

ALTER TABLE sys_system_attribute_mapping ALTER COLUMN attribute_cached SET NOT NULL;
ALTER TABLE sys_system_attribute_mapping ALTER COLUMN attribute_cached SET DEFAULT true;

ALTER TABLE sys_system_attribute_mapping_a ADD COLUMN cached_m boolean;
ALTER TABLE sys_system_attribute_mapping_a ADD COLUMN attribute_cached boolean;


