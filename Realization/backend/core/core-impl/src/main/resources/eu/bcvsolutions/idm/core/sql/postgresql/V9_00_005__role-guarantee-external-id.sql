--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- External id for core entities, which could be synchronized from external source

ALTER TABLE idm_role_guarantee ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_role_guarantee_ext_id
  ON idm_role_guarantee
  USING btree
  (external_id);
ALTER TABLE idm_role_guarantee_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_role_guarantee_a ADD COLUMN external_id_m boolean;


ALTER TABLE idm_role_guarantee_role ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_role_g_r_ext_id
  ON idm_role_guarantee_role
  USING btree
  (external_id);
ALTER TABLE idm_role_guarantee_role_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_role_guarantee_role_a ADD COLUMN external_id_m boolean;