--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Password history table and index

-- Table: idm_password_history

CREATE TABLE idm_password_history
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
  password character varying(255) NOT NULL,
  identity_id bytea NOT NULL,
  CONSTRAINT idm_password_history_pkey PRIMARY KEY (id)
);

-- Index: idx_idm_identity

CREATE INDEX idx_idm_identity
  ON idm_password_history
  USING btree
  (identity_id);

