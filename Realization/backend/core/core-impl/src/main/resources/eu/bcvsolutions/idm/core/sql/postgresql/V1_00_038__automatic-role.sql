--
-- CzechIdM 7.6 Flyway script 
-- BCV solutions s.r.o.
--
-- create table for automatic role (super class), new automatic role by attribute,
-- resolve old data in idm_role_tree_node

-- change constraint

ALTER TABLE idm_role_tree_node DROP CONSTRAINT ux_idm_role_tree_node;

-- create tables and index

CREATE TABLE idm_auto_role
(
  id bytea NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  creator_id bytea,
  modified timestamp without time zone,
  modifier character varying(255),
  modifier_id bytea,
  original_creator character varying(255),
  original_creator_id bytea,
  original_modifier character varying(255),
  original_modifier_id bytea,
  realm_id bytea,
  transaction_id bytea,
  name character varying(255) NOT NULL,
  role_id bytea NOT NULL,
  CONSTRAINT idm_auto_role_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_auto_role_name
  ON idm_auto_role
  USING btree
  (name);

CREATE INDEX idx_idm_auto_role_role
  ON idm_auto_role
  USING btree
  (role_id);

  CREATE TABLE idm_auto_role_a
  (
    id bytea NOT NULL,
    rev bigint NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    created_m boolean,
    creator character varying(255),
    creator_m boolean,
    creator_id bytea,
    creator_id_m boolean,
    modified timestamp without time zone,
    modified_m boolean,
    modifier character varying(255),
    modifier_m boolean,
    modifier_id bytea,
    modifier_id_m boolean,
    original_creator character varying(255),
    original_creator_m boolean,
    original_creator_id bytea,
    original_creator_id_m boolean,
    original_modifier character varying(255),
    original_modifier_m boolean,
    original_modifier_id bytea,
    original_modifier_id_m boolean,
    realm_id bytea,
    realm_id_m boolean,
    transaction_id bytea,
    transaction_id_m boolean,
    name character varying(255),
    name_m boolean,
    role_id bytea,
    role_m boolean,
    CONSTRAINT idm_auto_role_a_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fk_a5dnmuabje245da81j1mtv1id FOREIGN KEY (rev)
        REFERENCES idm_audit (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION
  );

CREATE TABLE idm_auto_role_att_rule
(
  id bytea NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  creator_id bytea,
  modified timestamp without time zone,
  modifier character varying(255),
  modifier_id bytea,
  original_creator character varying(255),
  original_creator_id bytea,
  original_modifier character varying(255),
  original_modifier_id bytea,
  realm_id bytea,
  transaction_id bytea,
  attribute_name character varying(255),
  comparison character varying(255) NOT NULL,
  type character varying(255) NOT NULL,
  value character varying(2000),
  auto_role_att_id bytea NOT NULL,
  form_attribute_id bytea,
  CONSTRAINT idm_auto_role_att_rule_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_auto_role_att_rule_id
  ON idm_auto_role_att_rule
  USING btree
  (auto_role_att_id);

CREATE INDEX idx_idm_auto_role_form_att_id
  ON idm_auto_role_att_rule
  USING btree
  (form_attribute_id);

CREATE INDEX idx_idm_auto_role_form_att_name
  ON idm_auto_role_att_rule
  USING btree
  (attribute_name);

CREATE INDEX idx_idm_auto_role_form_type
  ON idm_auto_role_att_rule
  USING btree
  (type);

CREATE TABLE idm_auto_role_att_rule_a
(
  id bytea NOT NULL,
  rev bigint NOT NULL,
  revtype smallint,
  created timestamp without time zone,
  created_m boolean,
  creator character varying(255),
  creator_m boolean,
  creator_id bytea,
  creator_id_m boolean,
  modified timestamp without time zone,
  modified_m boolean,
  modifier character varying(255),
  modifier_m boolean,
  modifier_id bytea,
  modifier_id_m boolean,
  original_creator character varying(255),
  original_creator_m boolean,
  original_creator_id bytea,
  original_creator_id_m boolean,
  original_modifier character varying(255),
  original_modifier_m boolean,
  original_modifier_id bytea,
  original_modifier_id_m boolean,
  realm_id bytea,
  realm_id_m boolean,
  transaction_id bytea,
  transaction_id_m boolean,
  attribute_name character varying(255),
  attribute_name_m boolean,
  comparison character varying(255),
  comparison_m boolean,
  type character varying(255),
  type_m boolean,
  value character varying(2000),
  value_m boolean,
  auto_role_att_id bytea,
  automatic_role_attribute_m boolean,
  form_attribute_id bytea,
  form_attribute_m boolean,
  CONSTRAINT idm_auto_role_att_rule_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_4h00l1b06fwkwi26fed2xq8p4 FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE idm_auto_role_attribute
(
  id bytea NOT NULL,
  CONSTRAINT idm_auto_role_attribute_pkey PRIMARY KEY (id),
  CONSTRAINT fk_b8r7j4ssop819j82ebm29kdaq FOREIGN KEY (id)
      REFERENCES idm_auto_role (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE idm_auto_role_attribute_a
(
  id bytea NOT NULL,
  rev bigint NOT NULL,
  CONSTRAINT idm_auto_role_attribute_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_otby9l02vverqso9ejrgeabj2 FOREIGN KEY (id, rev)
      REFERENCES idm_auto_role_a (id, rev) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- resolve data

INSERT INTO idm_auto_role (
  id,
  created,
  creator,
  creator_id,
  modified,
  modifier,
  modifier_id,
  original_creator,
  original_creator_id ,
  original_modifier,
  original_modifier_id,
  realm_id,
  transaction_id,
  name,
  role_id
) (
  SELECT 
    id,
    created,
    creator,
    creator_id,
    modified,
    modifier,
    modifier_id,
    original_creator,
    original_creator_id ,
    original_modifier,
    original_modifier_id,
    realm_id,
    transaction_id,
    'default',
    role_id
  FROM idm_role_tree_node
);

INSERT INTO idm_auto_role_a (
  id,
  rev,
  revtype,
  created,
  created_m,
  creator,
  creator_m,
  creator_id,
  creator_id_m,
  modified,
  modified_m,
  modifier,
  modifier_m,
  modifier_id,
  modifier_id_m,
  original_creator,
  original_creator_m,
  original_creator_id,
  original_creator_id_m,
  original_modifier,
  original_modifier_m,
  original_modifier_id,
  original_modifier_id_m,
  realm_id,
  realm_id_m,
  transaction_id,
  transaction_id_m,
  role_id,
  role_m
)(
  SELECT
    id,
    rev,
    revtype,
    created,
    created_m,
    creator,
    creator_m,
    creator_id,
    creator_id_m,
    modified,
    modified_m,
    modifier,
    modifier_m,
    modifier_id,
    modifier_id_m,
    original_creator,
    original_creator_m,
    original_creator_id,
    original_creator_id_m,
    original_modifier,
    original_modifier_m,
    original_modifier_id,
    original_modifier_id_m,
    realm_id,
    realm_id_m,
    transaction_id,
    transaction_id_m,
    role_id,
    role_m
  FROM idm_role_tree_node_a
);

-- drop old columns from idm role tree node

ALTER TABLE idm_role_tree_node DROP COLUMN created;
ALTER TABLE idm_role_tree_node DROP COLUMN creator;
ALTER TABLE idm_role_tree_node DROP COLUMN creator_id;
ALTER TABLE idm_role_tree_node DROP COLUMN modified;
ALTER TABLE idm_role_tree_node DROP COLUMN modifier;
ALTER TABLE idm_role_tree_node DROP COLUMN modifier_id;
ALTER TABLE idm_role_tree_node DROP COLUMN original_creator;
ALTER TABLE idm_role_tree_node DROP COLUMN original_creator_id;
ALTER TABLE idm_role_tree_node DROP COLUMN original_modifier;
ALTER TABLE idm_role_tree_node DROP COLUMN original_modifier_id;
ALTER TABLE idm_role_tree_node DROP COLUMN realm_id;
ALTER TABLE idm_role_tree_node DROP COLUMN transaction_id;
ALTER TABLE idm_role_tree_node DROP COLUMN role_id;

ALTER TABLE idm_role_tree_node_a DROP COLUMN revtype;
ALTER TABLE idm_role_tree_node_a DROP COLUMN created;
ALTER TABLE idm_role_tree_node_a DROP COLUMN created_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN creator;
ALTER TABLE idm_role_tree_node_a DROP COLUMN creator_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN creator_id;
ALTER TABLE idm_role_tree_node_a DROP COLUMN creator_id_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN modified;
ALTER TABLE idm_role_tree_node_a DROP COLUMN modified_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN modifier;
ALTER TABLE idm_role_tree_node_a DROP COLUMN modifier_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN modifier_id;
ALTER TABLE idm_role_tree_node_a DROP COLUMN modifier_id_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN original_creator;
ALTER TABLE idm_role_tree_node_a DROP COLUMN original_creator_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN original_creator_id;
ALTER TABLE idm_role_tree_node_a DROP COLUMN original_creator_id_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN original_modifier;
ALTER TABLE idm_role_tree_node_a DROP COLUMN original_modifier_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN original_modifier_id;
ALTER TABLE idm_role_tree_node_a DROP COLUMN original_modifier_id_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN realm_id;
ALTER TABLE idm_role_tree_node_a DROP COLUMN realm_id_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN transaction_id;
ALTER TABLE idm_role_tree_node_a DROP COLUMN transaction_id_m;
ALTER TABLE idm_role_tree_node_a DROP COLUMN role_id;
ALTER TABLE idm_role_tree_node_a DROP COLUMN role_m;

-- alter concept and idenitty role table

ALTER TABLE idm_identity_role DROP COLUMN automatic_role;

ALTER TABLE idm_identity_role_a DROP COLUMN automatic_role;

ALTER TABLE idm_identity_role_a DROP COLUMN automatic_role_m;

ALTER TABLE idm_concept_role_request RENAME COLUMN role_tree_node_id TO automatic_role_id;

ALTER TABLE idm_concept_role_request_a RENAME COLUMN role_tree_node_id TO automatic_role_id;

ALTER TABLE idm_concept_role_request_a RENAME COLUMN role_tree_node_m TO automatic_role_m;

DROP INDEX idx_idm_identity_role_aut_r;

ALTER TABLE idm_identity_role RENAME COLUMN role_tree_node_id TO automatic_role_id;

ALTER TABLE idm_identity_role_a RENAME COLUMN role_tree_node_id TO automatic_role_id;

ALTER TABLE idm_identity_role_a RENAME COLUMN role_tree_node_m TO automatic_role_m;

CREATE INDEX idx_idm_identity_role_aut_r
  ON idm_identity_role
  USING btree
  (automatic_role_id);
