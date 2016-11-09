-- Table idm_role_catalogue and idm_role_catalogue_aud
-- Add column roleCatalogue into idm_role
--

CREATE TABLE idm_role_catalogue
(
  id bigint NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  modified timestamp without time zone NOT NULL,
  modifier character varying(255),
  original_creator character varying(255),
  original_modifier character varying(255),
  name character varying(255) NOT NULL,
  description character varying(255),
  parent_id bigint,
  CONSTRAINT idm_role_catalogue_pkey PRIMARY KEY (id),
  CONSTRAINT fk_idm_role_catalogue_parent FOREIGN KEY (parent_id) REFERENCES idm_role_catalogue (id) ON DELETE CASCADE,
  CONSTRAINT ux_role_catalogue_name UNIQUE (name)
);

-- create audit table for idm_role_catalogue
CREATE TABLE idm_role_catalogue_aud
(
  id bigint NOT NULL,
  rev integer NOT NULL,
  revtype smallint,
  created timestamp without time zone,
  creator character varying(255),
  modified timestamp without time zone,
  modifier character varying(255),
  original_creator character varying(255),
  original_modifier character varying(255),
  name character varying(255),
  description character varying(255),
  parent_id bigint,
  CONSTRAINT idm_role_catalogue_aud_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_role_catalogue_revinfo_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)	
);

-- add new column role_catalogue_id into table idm_role
ALTER TABLE idm_role ADD COLUMN role_catalogue_id bigint;

-- add constraint to column role_catalogue_id
ALTER TABLE idm_role ADD CONSTRAINT fk_idm_role_catalogue FOREIGN KEY (role_catalogue_id) REFERENCES idm_role_catalogue (id) ON DELETE SET NULL;

-- add index to table idm_role
CREATE INDEX idx_idm_role_catalogue ON idm_role USING btree (role_catalogue_id);

-- add index to on table idm_role_catalogue
CREATE INDEX idx_idm_role_catalogue_parent ON idm_role_catalogue USING btree (parent_id);

-- Audit tables
-- add new column role_catalogue_id into table idm_role_aud
ALTER TABLE idm_role_aud ADD COLUMN role_catalogue_id bigint;

