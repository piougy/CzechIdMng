--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
-- Add table for realation bettwen Accoutn and Tree

-- Table: acc_tree_account

CREATE TABLE acc_tree_account
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
  ownership boolean NOT NULL,
  account_id bytea NOT NULL,
  role_system_id bytea,
  tree_node_id bytea NOT NULL,
  CONSTRAINT acc_tree_account_pkey PRIMARY KEY (id)
);

-- Table: acc_tree_account_a


CREATE TABLE acc_tree_account_a
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
  ownership boolean,
  ownership_m boolean,
  account_id bytea,
  account_m boolean,
  role_system_id bytea,
  role_system_m boolean,
  tree_node_id bytea,
  tree_node_m boolean,
  CONSTRAINT acc_tree_account_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_5c3jg826m2x2fq1q677uwk02u FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


-- Add column: roots_filter_script
ALTER TABLE sys_sync_config ADD COLUMN roots_filter_script text;

-- Add audit columns: roots_filter_script
ALTER TABLE sys_sync_config_a ADD COLUMN roots_filter_script text;
ALTER TABLE sys_sync_config_a ADD COLUMN roots_filter_script_m boolean;
