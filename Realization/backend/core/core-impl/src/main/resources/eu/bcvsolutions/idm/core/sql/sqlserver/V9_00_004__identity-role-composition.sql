--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Business role references int identity role

ALTER TABLE idm_identity_role ADD direct_role_id binary(16);
ALTER TABLE idm_identity_role_a ADD direct_role_id binary(16);
ALTER TABLE idm_identity_role_a ADD direct_role_m bit;

CREATE INDEX idx_idm_identity_role_d_r_id
  ON idm_identity_role(direct_role_id);
  
ALTER TABLE idm_identity_role ADD role_composition_id binary(16);
ALTER TABLE idm_identity_role_a ADD role_composition_id binary(16);
ALTER TABLE idm_identity_role_a ADD role_composition_m bit;

CREATE INDEX idx_idm_identity_role_comp_id
  ON idm_identity_role(role_composition_id);
