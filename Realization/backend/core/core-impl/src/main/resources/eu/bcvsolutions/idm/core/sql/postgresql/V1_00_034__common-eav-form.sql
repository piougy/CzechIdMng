--
-- CzechIdM 7.6 Flyway script 
-- BCV solutions s.r.o.
--
-- common eav forms

----- TABLE idm_form -----
CREATE TABLE idm_form
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
  name character varying(255),
  owner_id bytea,
  owner_type character varying(255) NOT NULL,
  owner_code character varying(255),
  form_definition_id bytea NOT NULL,
  CONSTRAINT idm_form_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_form_owner_id
  ON idm_form
  USING btree
  (owner_id);

CREATE INDEX idx_idm_form_owner_type
  ON idm_form
  USING btree
  (owner_type);

CREATE INDEX idx_idm_form_owner_code
  ON idm_form
  USING btree
  (owner_code);
  
CREATE INDEX idx_idm_form_f_definition_id
  ON idm_form
  USING btree
  (form_definition_id);
  
  
----- TABLE idm_form_a -----
CREATE TABLE idm_form_a
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
  owner_id bytea,
  owner_id_m boolean,
  owner_type character varying(255),
  owner_type_m boolean,
  owner_code character varying(255),
  owner_code_m boolean,
  form_definition_id bytea,
  form_definition_m boolean,
  CONSTRAINT idm_form_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_idm_form_rev FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
  
-- persists values in eav
CREATE TABLE idm_form_value
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
  uuid_value bytea,
  CONSTRAINT idm_form_value_pkey PRIMARY KEY (id),
  CONSTRAINT idm_form_value_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_idm_form_value_a
  ON idm_form_value
  USING btree
  (owner_id);

CREATE INDEX idx_idm_form_value_a_def
  ON idm_form_value
  USING btree
  (attribute_id);

CREATE INDEX idx_idm_form_value_a_str
  ON idm_form_value
  USING btree
  (string_value);

CREATE TABLE idm_form_value_a
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
  uuid_value bytea,
  uuid_value_m boolean,
  CONSTRAINT idm_form_value_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_idm_form_value_rev FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
