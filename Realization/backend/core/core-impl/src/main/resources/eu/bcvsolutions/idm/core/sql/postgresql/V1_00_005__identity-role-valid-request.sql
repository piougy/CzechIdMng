--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required table for object IdmIdentityRoleValidRequest


CREATE TABLE idm_identity_role_valid_req
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
  current_attempt integer,
  result_cause text,
  result_code character varying(255),
  result_model bytea,
  result_state character varying(45) NOT NULL,
  identity_role_id bytea NOT NULL,
  CONSTRAINT idm_identity_role_valid_request_pkey PRIMARY KEY (id),
  CONSTRAINT idx_idm_identity_role_id UNIQUE (identity_role_id)
)
