--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Create authority change table.

CREATE TABLE idm_authority_change
(
  id bytea NOT NULL,
  auth_change_timestamp timestamp without time zone NOT NULL,
  identity_id bytea NOT NULL,
  CONSTRAINT idm_authority_change_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_authority_change_identity
  ON idm_authority_change
  USING btree
  (identity_id);
