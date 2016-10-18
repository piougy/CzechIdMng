-- rename working positions to contract and link them to new tree node type structure

ALTER TABLE idm_identity_working_position RENAME TO idm_identity_contract;
ALTER TABLE idm_identity_working_position_aud RENAME TO idm_identity_contract_aud;

-- manager will be garant
ALTER TABLE idm_identity_contract RENAME COLUMN manager_id TO garant_id;
ALTER TABLE idm_identity_contract_aud RENAME COLUMN manager_id TO garant_id;

-- tree node will be working position
ALTER TABLE idm_identity_contract RENAME COLUMN tree_node_id TO working_position_id;
ALTER TABLE idm_identity_contract_aud RENAME COLUMN tree_node_id TO working_position_id;
ALTER TABLE idm_identity_contract DROP COLUMN position;