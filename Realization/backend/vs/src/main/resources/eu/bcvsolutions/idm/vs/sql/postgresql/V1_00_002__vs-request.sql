-- Table: vs_request

CREATE TABLE vs_request
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
  connector_conf bytea,
  connector_key character varying(255) NOT NULL,
  connector_object bytea,
  execute_immediately boolean NOT NULL,
  operation_type character varying(255) NOT NULL,
  state character varying(255) NOT NULL,
  system_id bytea NOT NULL,
  uid character varying(255) NOT NULL,
  duplicate_to_request_id bytea,
  previous_request_id bytea,
  CONSTRAINT vs_request_pkey PRIMARY KEY (id)
);

-- Table: vs_request_a

CREATE TABLE vs_request_a
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
  connector_conf bytea,
  configuration_m boolean,
  connector_key character varying(255),
  connector_key_m boolean,
  connector_object bytea,
  connector_object_m boolean,
  execute_immediately boolean,
  execute_immediately_m boolean,
  operation_type character varying(255),
  operation_type_m boolean,
  state character varying(255),
  state_m boolean,
  system_id bytea,
  system_id_m boolean,
  uid character varying(255),
  uid_m boolean,
  CONSTRAINT vs_request_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_request_a_idm_audit FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Table: vs_request_implementer

CREATE TABLE vs_request_implementer
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
  identity_id bytea NOT NULL,
  request_id bytea NOT NULL,
  CONSTRAINT vs_request_implementer_pkey PRIMARY KEY (id)
);

-- Index: idx_vs_request_imp_req

CREATE INDEX idx_vs_request_imp_req
  ON vs_request_implementer
  USING btree
  (request_id);


-- Table: vs_request_implementer_a

CREATE TABLE vs_request_implementer_a
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
  identity_id bytea,
  identity_m boolean,
  request_id bytea,
  request_m boolean,
  CONSTRAINT vs_request_implementer_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_request_imp_a_idm_audit FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

