--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Add new indexes for IdmConceptRoleRequest

CREATE INDEX idx_idm_conc_role_iden_rol
  ON idm_concept_role_request
  USING btree
  (identity_role_id);
  
CREATE INDEX idx_idm_conc_role_tree_node
  ON idm_concept_role_request
  USING btree
  (role_tree_node_id);
