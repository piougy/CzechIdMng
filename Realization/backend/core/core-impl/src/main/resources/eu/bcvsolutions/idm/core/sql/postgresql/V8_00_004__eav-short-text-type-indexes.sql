--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- add new persistent type fot indexed short text

ALTER TABLE idm_identity_form_value ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_identity_form_value_a ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_identity_form_value_a ADD COLUMN short_text_value_m boolean;

CREATE INDEX idx_idm_identity_form_stxt
  ON idm_identity_form_value
  USING btree
  (short_text_value);
CREATE INDEX idx_idm_identity_form_uuid
  ON idm_identity_form_value
  USING btree
  (uuid_value);
--
ALTER TABLE idm_role_form_value ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_role_form_value_a ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_role_form_value_a ADD COLUMN short_text_value_m boolean;

CREATE INDEX idx_idm_role_form_stxt
  ON idm_role_form_value
  USING btree
  (short_text_value);
CREATE INDEX idx_idm_role_form_uuid
  ON idm_role_form_value
  USING btree
  (uuid_value);
--
ALTER TABLE idm_i_contract_form_value ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_i_contract_form_value_a ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_i_contract_form_value_a ADD COLUMN short_text_value_m boolean;

CREATE INDEX idx_idm_i_contract_form_stxt
  ON idm_i_contract_form_value
  USING btree
  (short_text_value);
CREATE INDEX idx_idm_i_contract_form_uuid
  ON idm_i_contract_form_value
  USING btree
  (uuid_value);
--
ALTER TABLE idm_tree_node_form_value ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_tree_node_form_value_a ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_tree_node_form_value_a ADD COLUMN short_text_value_m boolean;

CREATE INDEX idx_idm_tree_node_form_stxt
  ON idm_tree_node_form_value
  USING btree
  (short_text_value);
CREATE INDEX idx_idm_tree_node_form_uuid
  ON idm_tree_node_form_value
  USING btree
  (uuid_value);
--
ALTER TABLE idm_form_value ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_form_value_a ADD COLUMN short_text_value character varying(2000);
ALTER TABLE idm_form_value_a ADD COLUMN short_text_value_m boolean;

CREATE INDEX idx_idm_form_value_stxt
  ON idm_form_value
  USING btree
  (short_text_value);
CREATE INDEX idx_idm_form_value_uuid
  ON idm_form_value
  USING btree
  (uuid_value);

