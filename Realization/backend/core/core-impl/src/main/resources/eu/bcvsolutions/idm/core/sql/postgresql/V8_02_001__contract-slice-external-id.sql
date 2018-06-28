--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Contract slice extenal id

ALTER TABLE idm_contract_slice ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_contract_slice_ext_id
  ON idm_contract_slice
  USING btree
  (external_id);
ALTER TABLE idm_contract_slice_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_contract_slice_a ADD COLUMN external_id_m boolean;
