--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--

ALTER TABLE sys_system_mapping
   ADD COLUMN tree_type_id bytea;
   
ALTER TABLE sys_system_mapping_a
   ADD COLUMN tree_type_id bytea;
   
ALTER TABLE sys_system_mapping_a
   ADD COLUMN tree_type_m boolean;