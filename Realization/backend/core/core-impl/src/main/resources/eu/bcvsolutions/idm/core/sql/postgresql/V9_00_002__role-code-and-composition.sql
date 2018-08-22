--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add code to role
-- Add role composition
--
ALTER TABLE idm_role ADD COLUMN code character varying(255);
ALTER TABLE idm_role_a ADD COLUMN code character varying(255);
ALTER TABLE idm_role_a ADD COLUMN code_m boolean;
-- code = name by default
UPDATE idm_role set code = name;
-- add not null
ALTER TABLE idm_role ALTER COLUMN code SET NOT NULL;
-- drop original ux constraint
ALTER TABLE idm_role DROP CONSTRAINT ux_idm_role_name;
-- create new
CREATE INDEX idx_idm_role_name
  ON idm_role
  USING btree
  (name);
CREATE UNIQUE INDEX ux_idm_role_code 
  ON idm_role 
  USING btree
  (code);
--
-- add composition index
ALTER TABLE idm_role_composition ADD CONSTRAINT ux_idm_role_composition_susu UNIQUE (superior_id,sub_id);


