--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Persistent IdM tokens

-- drop table idm_authority_change
drop table idm_authority_change;

CREATE TABLE idm_token (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea NULL,
	modified timestamp NULL,
	modifier varchar(255) NULL,
	modifier_id bytea NULL,
	original_creator varchar(255) NULL,
	original_creator_id bytea NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id bytea NULL,
	realm_id bytea NULL,
	transaction_id bytea NULL,
	disabled bool NOT NULL,
	expiration timestamp NULL,
	issued_at timestamp NOT NULL,
	module_id varchar(255) NULL,
	owner_id bytea NULL,
	owner_type varchar(255) NOT NULL,
	properties bytea NULL,
	token varchar(2000) NULL,
	token_type varchar(45) NULL,
	CONSTRAINT idm_token_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_token_exp ON idm_token USING btree (expiration);
CREATE INDEX idx_idm_token_o_id ON idm_token USING btree (owner_id);
CREATE INDEX idx_idm_token_o_type ON idm_token USING btree (owner_type);
CREATE INDEX idx_idm_token_token ON idm_token USING btree (token);
CREATE INDEX idx_idm_token_type ON idm_token USING btree (token_type);

ALTER TABLE idm_token ADD COLUMN external_id character varying(255);
CREATE INDEX idx_idm_token_external_id
  ON idm_token
  USING btree
  (external_id);

