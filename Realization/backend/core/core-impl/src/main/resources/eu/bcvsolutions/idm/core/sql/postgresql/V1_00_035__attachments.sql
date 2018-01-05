--
-- CzechIdM 7.6 Flyway script 
-- BCV solutions s.r.o.
--
-- attachments

CREATE TABLE idm_attachment
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
  attachment_type character varying(50),
  content_id bytea NOT NULL,
  content_path character varying(512),
  description character varying(2000),
  encoding character varying(100) NOT NULL,
  filesize bigint NOT NULL,
  mimetype character varying(255) NOT NULL,
  name character varying(255) NOT NULL,
  owner_id bytea,
  owner_state character varying(50),
  owner_type character varying(255) NOT NULL,
  version_label character varying(10) NOT NULL,
  version_number integer NOT NULL,
  next_version_id bytea,
  parent_id bytea,
  CONSTRAINT idm_attachment_pkey PRIMARY KEY (id),
  CONSTRAINT idm_attachment_filesize_check CHECK (filesize <= 999999999999999999::bigint),
  CONSTRAINT idm_attachment_version_number_check CHECK (version_number >= 1 AND version_number <= 9999)
);

CREATE INDEX idx_idm_attachment_desc
  ON idm_attachment
  USING btree
  (description);

CREATE INDEX idx_idm_attachment_name
  ON idm_attachment
  USING btree
  (name);

CREATE INDEX idx_idm_attachment_o_id
  ON idm_attachment
  USING btree
  (owner_id);

CREATE INDEX idx_idm_attachment_o_type
  ON idm_attachment
  USING btree
  (owner_type);
