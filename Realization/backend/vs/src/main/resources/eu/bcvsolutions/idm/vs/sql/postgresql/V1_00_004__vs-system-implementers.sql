-- Remove vs request-implementer and create vs system-implementer

DROP TABLE vs_request_implementer;
DROP TABLE vs_request_implementer_a;
 
CREATE TABLE vs_system_implementer
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
  identity_id bytea,
  role_id bytea,
  system_id bytea NOT NULL,
  CONSTRAINT vs_system_implementer_pkey PRIMARY KEY (id)
);

CREATE TABLE vs_system_implementer_a
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
  role_id bytea,
  role_m boolean,
  system_id bytea,
  system_m boolean,
  CONSTRAINT vs_system_implementer_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_ar40qritwofj5acof4vk9l6bq FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
