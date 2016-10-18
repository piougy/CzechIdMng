-- add tree node code
ALTER TABLE idm_tree_node ADD COLUMN code character varying(255);
ALTER TABLE idm_tree_node_aud ADD COLUMN code character varying(255);
UPDATE idm_tree_node SET code = id;
ALTER TABLE idm_tree_node ALTER code SET NOT NULL;
ALTER TABLE ONLY idm_tree_node ADD CONSTRAINT ux_tree_node_code UNIQUE (code);

-- drop unique index on name - name could be the same in diferent tree depth and for diferent tree type
DROP INDEX ux_tree_node_name;