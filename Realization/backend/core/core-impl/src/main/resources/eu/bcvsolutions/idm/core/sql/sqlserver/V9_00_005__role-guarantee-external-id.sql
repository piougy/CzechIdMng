--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- External id for core entities, which could be synchronized from external source

ALTER TABLE idm_role_guarantee ADD external_id nvarchar(255);
CREATE INDEX idx_idm_role_guarantee_ext_id
  ON idm_role_guarantee(external_id);
ALTER TABLE idm_role_guarantee_a ADD external_id nvarchar(255);
ALTER TABLE idm_role_guarantee_a ADD external_id_m bit;


ALTER TABLE idm_role_guarantee_role ADD external_id nvarchar(255);
CREATE INDEX idx_idm_role_g_r_ext_id
  ON idm_role_guarantee_role(external_id);
ALTER TABLE idm_role_guarantee_role_a ADD external_id nvarchar(255);
ALTER TABLE idm_role_guarantee_role_a ADD external_id_m bit;