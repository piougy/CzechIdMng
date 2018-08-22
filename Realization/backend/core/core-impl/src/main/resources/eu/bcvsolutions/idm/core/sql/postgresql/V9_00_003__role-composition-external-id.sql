--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- External id for core entities, which could be synchronized from external source

ALTER TABLE idm_role_composition ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_role_composition_e_id
  ON idm_role_composition
  USING btree
  (external_id);
ALTER TABLE idm_role_composition_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_role_composition_a ADD COLUMN external_id_m boolean;
