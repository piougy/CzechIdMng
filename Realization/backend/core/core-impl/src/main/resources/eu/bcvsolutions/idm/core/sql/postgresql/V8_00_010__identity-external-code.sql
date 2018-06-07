--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- External code for identity

ALTER TABLE idm_identity ADD COLUMN external_code character varying(255);

CREATE INDEX idx_idm_identity_external_code
  ON idm_identity
  USING btree
  (external_code);

ALTER TABLE idm_identity_a ADD COLUMN external_code character varying(255);

ALTER TABLE idm_identity_a ADD COLUMN external_code_m boolean;
