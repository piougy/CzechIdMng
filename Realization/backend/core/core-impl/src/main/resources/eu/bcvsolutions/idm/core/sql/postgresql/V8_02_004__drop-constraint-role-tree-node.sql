--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Drop constraint for role tree node

ALTER TABLE idm_role_tree_node DROP CONSTRAINT ux_idm_role_tree_node;
