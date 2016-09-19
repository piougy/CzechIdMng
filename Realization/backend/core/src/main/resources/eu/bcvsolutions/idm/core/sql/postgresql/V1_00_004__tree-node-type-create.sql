-- This SQL script updates organization to tree_node and create table tree_type


-- rename table from organization to tree_node
ALTER TABLE idm_organization RENAME TO idm_tree_node;

-- drop
ALTER TABLE idm_identity_working_position DROP CONSTRAINT fk_objykl2b6pnho13ao7bnppewa;
ALTER TABLE idm_tree_node DROP CONSTRAINT fk_2n1u7nfur91wasj0csui0jbhs;
ALTER TABLE idm_tree_node DROP CONSTRAINT idm_organization_pkey;

-- create PK on idm_tree_node
ALTER TABLE idm_tree_node ADD CONSTRAINT idm_tree_node_pkey PRIMARY KEY (id);

-- create FK on idm_tree_node
ALTER TABLE idm_tree_node ADD CONSTRAINT fk_idm_tree_node_parent_id FOREIGN KEY (parent_id) REFERENCES idm_tree_node(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

-- rename index on idm_tree_node
ALTER INDEX ux_organization_name RENAME TO ux_tree_node_name;

-- rename column and constraint on identity_working_position
ALTER TABLE idm_identity_working_position RENAME COLUMN organization_id TO tree_node_id;
ALTER TABLE idm_identity_working_position ADD CONSTRAINT fk_idm_identity_working_position_tree_node_id FOREIGN KEY (tree_node_id) REFERENCES idm_tree_node (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

-- create new table tree_type and fk to tree_node and index
CREATE TABLE idm_tree_type
(
  id bigint NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  modified timestamp without time zone NOT NULL,
  modifier character varying(255),
  original_creator character varying(255),
  original_modifier character varying(255),
  name character varying(255) NOT NULL,
  CONSTRAINT idm_tree_type_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);


CREATE INDEX ux_idm_tree_type_name
  ON idm_tree_type
  USING btree
  (name COLLATE pg_catalog."default");

-- add column on idm_tree_node, FK to idm_tree_type
ALTER TABLE idm_tree_node ADD COLUMN tree_type_id bigint;
ALTER TABLE idm_tree_node ADD CONSTRAINT fk_idm_tree_node_tree_type_id FOREIGN KEY (tree_type_id) REFERENCES idm_tree_type (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;


-- Next sql queries is for AUD tables
--
-- fix audit table, and create audit table for idm_tree_type
-- rename table
ALTER TABLE idm_organization_aud RENAME TO idm_tree_node_aud;

-- drop
ALTER TABLE idm_tree_node_aud DROP CONSTRAINT fk_pdll1wf2b1e1tg2c3nr8rvdnf;
ALTER TABLE idm_tree_node_aud DROP CONSTRAINT idm_organization_aud_pkey;

-- create PK on idm_tree_node_aud
ALTER TABLE idm_tree_node_aud ADD CONSTRAINT idm_tree_node_aud_pkey PRIMARY KEY (id, rev);

-- add column on idm_tree_node
ALTER TABLE idm_tree_node_aud ADD COLUMN tree_type_id bigint;

ALTER TABLE idm_identity_working_position_aud RENAME COLUMN organization_id TO tree_node_id;

-- create new table tree_type_aud
CREATE TABLE idm_tree_type_aud
(
  id bigint NOT NULL,
  rev integer NOT NULL,
  revtype smallint,
  created timestamp without time zone,
  creator character varying(255),
  modified timestamp without time zone,
  modifier character varying(255),
  original_creator character varying(255),
  original_modifier character varying(255),
  name character varying(255),
  CONSTRAINT idm_tree_type_aud_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_revinfo_rev FOREIGN KEY (rev)
      REFERENCES revinfo (rev) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

-- create default type and add to all nodes
INSERT INTO idm_tree_type(id, name, created, creator) VALUES ((SELECT nextval('hibernate_sequence')), 'ORGANIZATIONS', (SELECT current_time), '[SYSTEM]');
UPDATE idm_tree_node SET tree_type_id = (SELECT id FROM idm_tree_type WHERE name = 'ORGANIZATIONS');

