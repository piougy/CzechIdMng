--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Add column External ID to tree node

ALTER TABLE idm_tree_node ADD COLUMN external_id character varying(255);
ALTER TABLE idm_tree_node_a ADD COLUMN external_id character varying(255);
ALTER TABLE idm_tree_node_a ADD COLUMN external_id_m boolean;
