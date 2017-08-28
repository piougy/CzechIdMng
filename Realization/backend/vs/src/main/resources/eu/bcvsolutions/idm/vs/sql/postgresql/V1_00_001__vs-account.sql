-- Create vs_account;

CREATE TABLE vs_account
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
  connector_key character varying(255) NOT NULL,
  enable boolean NOT NULL,
  system_id bytea NOT NULL,
  uid character varying(255) NOT NULL,
  CONSTRAINT vs_account_pkey PRIMARY KEY (id),
  CONSTRAINT ux_vs_account_uid UNIQUE (uid, system_id, connector_key)
);


-- Create audit vs_account_a;

CREATE TABLE vs_account_a
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
  connector_key character varying(255),
  connector_key_m boolean,
  enable boolean,
  enable_m boolean,
  system_id bytea,
  system_id_m boolean,
  uid character varying(255),
  uid_m boolean,
  CONSTRAINT vs_account_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_vs_acc_a_idm_audit FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


-- Create vs_account_form_value;

CREATE TABLE vs_account_form_value
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
  boolean_value boolean,
  byte_value bytea,
  confidential boolean NOT NULL,
  date_value timestamp without time zone,
  double_value numeric(38,4),
  long_value bigint,
  persistent_type character varying(45) NOT NULL,
  seq smallint,
  string_value text,
  attribute_id bytea NOT NULL,
  owner_id bytea NOT NULL,
  CONSTRAINT vs_account_form_value_pkey PRIMARY KEY (id),
  CONSTRAINT vs_account_form_value_seq_check CHECK (seq <= 99999)
);

-- Index: idx_vs_account_form_a

CREATE INDEX idx_vs_account_form_a
  ON vs_account_form_value
  USING btree
  (owner_id);

-- Index: idx_vs_account_form_a_def

CREATE INDEX idx_vs_account_form_a_def
  ON vs_account_form_value
  USING btree
  (attribute_id);
  
  
-- Create audit vs_account_form_value_a;

CREATE TABLE vs_account_form_value_a
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
  boolean_value boolean,
  boolean_value_m boolean,
  byte_value bytea,
  byte_value_m boolean,
  confidential boolean,
  confidential_m boolean,
  date_value timestamp without time zone,
  date_value_m boolean,
  double_value numeric(38,4),
  double_value_m boolean,
  long_value bigint,
  long_value_m boolean,
  persistent_type character varying(45),
  persistent_type_m boolean,
  seq smallint,
  seq_m boolean,
  string_value text,
  string_value_m boolean,
  attribute_id bytea,
  form_attribute_m boolean,
  owner_id bytea,
  owner_m boolean,
  CONSTRAINT vs_account_form_value_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_vs_acc_fvalue_a_audit FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

