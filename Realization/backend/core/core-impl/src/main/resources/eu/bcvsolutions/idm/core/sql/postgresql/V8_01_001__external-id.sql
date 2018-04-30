--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- External id for code entities, which could be synchronized from external source

ALTER TABLE idm_identity ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_identity_external_id
  ON idm_identity
  USING btree
  (external_id);
ALTER TABLE idm_identity_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_identity_a ADD COLUMN external_id_m boolean;
--
ALTER TABLE idm_role ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_role_external_id
  ON idm_role
  USING btree
  (external_id);

ALTER TABLE idm_role_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_role_a ADD COLUMN external_id_m boolean;
--
ALTER TABLE idm_identity_contract ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_contract_ext_id
  ON idm_identity_contract
  USING btree
  (external_id);
ALTER TABLE idm_identity_contract_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_identity_contract_a ADD COLUMN external_id_m boolean;
--
ALTER TABLE idm_contract_guarantee ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_contract_guar_ext_id
  ON idm_contract_guarantee
  USING btree
  (external_id);
ALTER TABLE idm_contract_guarantee_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_contract_guarantee_a ADD COLUMN external_id_m boolean;
--
ALTER TABLE idm_identity_role ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_identity_role_ext_id
  ON idm_identity_role
  USING btree
  (external_id);
ALTER TABLE idm_identity_role_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_identity_role_a ADD COLUMN external_id_m boolean;
--
ALTER TABLE idm_tree_type ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_tree_type_ext_id
  ON idm_tree_type
  USING btree
  (external_id);
ALTER TABLE idm_tree_type_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_tree_type_a ADD COLUMN external_id_m boolean;
-- tree node has externalId already
CREATE INDEX idx_idm_tree_node_ext_id
  ON idm_tree_node
  USING btree
  (external_id);
--
ALTER TABLE idm_role_catalogue ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_role_cat_ext_id
  ON idm_role_catalogue
  USING btree
  (external_id);
ALTER TABLE idm_role_catalogue_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_role_catalogue_a ADD COLUMN external_id_m boolean;

