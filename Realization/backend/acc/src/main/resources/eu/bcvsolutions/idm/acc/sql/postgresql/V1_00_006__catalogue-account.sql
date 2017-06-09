--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
-- Add table for realation bettwen Account and Role catalogue

-- Table: acc_role_catalogue_account

CREATE TABLE acc_role_catalogue_account
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
  role_catalogue_id bytea NOT NULL,
  role_system_id bytea,
  CONSTRAINT acc_role_catalogue_account_pkey PRIMARY KEY (id)
);

-- Index: idx_acc_cat_account_acc

CREATE INDEX idx_acc_cat_account_acc
  ON acc_role_catalogue_account
  USING btree
  (account_id);

-- Index: idx_acc_cat_account_tree

CREATE INDEX idx_acc_cat_account_tree
  ON acc_role_catalogue_account
  USING btree
  (role_catalogue_id);

  
  -- Table: acc_role_catalogue_account_a

CREATE TABLE acc_role_catalogue_account_a
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
  role_catalogue_id bytea,
  role_catalogue_m boolean,
  role_system_id bytea,
  role_system_m boolean,
  CONSTRAINT acc_role_catalogue_account_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_civ47mx4nlpsvlro1r7nw7ycr FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


