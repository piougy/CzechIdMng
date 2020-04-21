--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add role guarantee type attribute

-- type guarantee-identity
ALTER TABLE idm_role_guarantee ADD COLUMN guarantee_type character varying(255);
ALTER TABLE idm_role_guarantee_a ADD COLUMN guarantee_type character varying(255);
ALTER TABLE idm_role_guarantee_a ADD COLUMN type_m boolean;
-- index
CREATE INDEX idx_idm_role_guarantee_type
  ON idm_role_guarantee
  USING btree
  (guarantee_type);

-- type guarantee-role
ALTER TABLE idm_role_guarantee_role ADD COLUMN guarantee_type character varying(255);
ALTER TABLE idm_role_guarantee_role_a ADD COLUMN guarantee_type character varying(255);
ALTER TABLE idm_role_guarantee_role_a ADD COLUMN type_m boolean;
-- index
CREATE INDEX idx_idm_role_g_r_type
  ON idm_role_guarantee_role
  USING btree
  (guarantee_type);