--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Add table for IdmScriptAuthority


CREATE TABLE idm_script_authority
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
  service character varying(255),
  class_name character varying(255),
  script_id bytea NOT NULL,
  type character varying(255) NOT NULL,
  CONSTRAINT idm_script_authority_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_script_auth_script
  ON idm_script_authority
  USING btree
  (script_id);

CREATE TABLE idm_script_authority_a
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
  service character varying(255),
  service_m boolean,
  class_name character varying(255),
  class_name_m boolean,
  type character varying(255),
  type_m boolean,
  script_id bytea,
  script_m boolean,
  CONSTRAINT idm_script_authority_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_idm_script_authority_a_rev FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

alter table idm_script add column code character varying(255) NOT NULL;

alter table idm_script add constraint ux_script_code unique (code);

alter table idm_script_a add column code character varying(255);

alter table idm_script_a add column code_m boolean;
