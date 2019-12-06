--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add role environment attribute

-- environment
ALTER TABLE idm_role ADD COLUMN environment character varying(255);
ALTER TABLE idm_role_a ADD COLUMN environment character varying(255);
ALTER TABLE idm_role_a ADD COLUMN environment_m boolean;
-- index
CREATE INDEX idx_idm_role_environment
  ON idm_role
  USING btree
  (environment);
-- base code
ALTER TABLE idm_role ADD COLUMN base_code character varying(255);
ALTER TABLE idm_role_a ADD COLUMN base_code character varying(255);
ALTER TABLE idm_role_a ADD COLUMN base_code_m boolean;
-- index
CREATE INDEX idx_idm_role_base_code
  ON idm_role
  USING btree
  (base_code);
UPDATE idm_role SET base_code = code;
ALTER TABLE idm_role ALTER COLUMN base_code SET NOT NULL;



