--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script adds authorization policies

CREATE TABLE idm_authorization_policy
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
  authorizable_type character varying(255),
  base_permissions character varying(255),
  description character varying(2000),
  disabled boolean NOT NULL,
  evaluator_properties bytea,
  evaluator_type character varying(255) NOT NULL,
  seq smallint,
  role_id bytea NOT NULL,
  CONSTRAINT idm_authorization_policy_pkey PRIMARY KEY (id),
  CONSTRAINT idm_authorization_policy_seq_check CHECK (seq <= 99999)
);

CREATE TABLE idm_authorization_policy_a
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
  authorizable_type character varying(255),
  authorizable_type_m boolean,
  base_permissions character varying(255),
  base_permissions_m boolean,
  description character varying(2000),
  description_m boolean,
  disabled boolean,
  disabled_m boolean,
  evaluator_properties bytea,
  evaluator_properties_m boolean,
  evaluator_type character varying(255),
  evaluator_type_m boolean,
  seq smallint,
  seq_m boolean,
  role_id bytea,
  role_m boolean,
  CONSTRAINT idm_authorization_policy_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_gobi05mdqmnq11h8n409tsqmm FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX idx_idm_author_policy_a_t
  ON idm_authorization_policy
  USING btree
  (authorizable_type);

CREATE INDEX idx_idm_author_policy_role
  ON idm_authorization_policy
  USING btree
  (role_id);
  
