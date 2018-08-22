--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Business role references int identity role

ALTER TABLE idm_identity_role ADD COLUMN direct_role_id bytea;
ALTER TABLE idm_identity_role_a ADD COLUMN direct_role_id bytea;
ALTER TABLE idm_identity_role_a ADD COLUMN direct_role_m boolean;

CREATE INDEX idx_idm_identity_role_d_r_id
  ON idm_identity_role
  USING btree
  (direct_role_id);
  
ALTER TABLE idm_identity_role ADD COLUMN role_composition_id bytea;
ALTER TABLE idm_identity_role_a ADD COLUMN role_composition_id bytea;
ALTER TABLE idm_identity_role_a ADD COLUMN role_composition_m boolean;

CREATE INDEX idx_idm_identity_role_comp_id
  ON idm_identity_role
  USING btree
  (role_composition_id);
