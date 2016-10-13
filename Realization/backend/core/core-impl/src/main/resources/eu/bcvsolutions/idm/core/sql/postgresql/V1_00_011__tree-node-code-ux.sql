-- drop unique index on name - name could be the same in diferent tree depth and for diferent tree type
ALTER TABLE ONLY idm_tree_node DROP CONSTRAINT ux_tree_node_code;
ALTER TABLE ONLY idm_tree_node ADD CONSTRAINT ux_tree_node_code UNIQUE (tree_type_id,code);