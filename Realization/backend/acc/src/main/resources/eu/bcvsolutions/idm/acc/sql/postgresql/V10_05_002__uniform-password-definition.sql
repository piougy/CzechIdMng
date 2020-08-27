--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Uniform password definition and password filter definition for system atribute mapping


CREATE TABLE acc_uniform_password
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
  code character varying(255),
  description character varying(2000),
  disabled boolean NOT NULL,
  change_in_idm boolean NOT NULL,
  CONSTRAINT acc_uniform_password_pkey PRIMARY KEY (id),
  CONSTRAINT ux_acc_uniform_password_code UNIQUE (code)
);

CREATE TABLE acc_uniform_password_a
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
  code character varying(255),
  code_m boolean,
  description character varying(2000),
  description_m boolean,
  disabled boolean,
  disabled_m boolean,
  change_in_idm boolean,
  change_in_idm_m boolean,
  CONSTRAINT acc_uniform_password_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_acc_uniform_password_rev FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE acc_uniform_password_system
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
  system_id bytea NOT NULL,
  uniform_password_id bytea NOT NULL,
  CONSTRAINT acc_uniform_password_sys_pkey PRIMARY KEY (id),
  CONSTRAINT ux_acc_uniform_pass_id_sys_id UNIQUE (system_id,uniform_password_id)
);
 
CREATE INDEX idx_sys_system_id
  ON acc_uniform_password_system
  USING btree
  (system_id);

CREATE INDEX idx_acc_uniform_password_id
  ON acc_uniform_password_system
  USING btree
  (uniform_password_id);

CREATE TABLE acc_uniform_password_system_a
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
  system_id bytea,
  system_m boolean,
  uniform_password_id bytea,
  uniform_password_m boolean,
  CONSTRAINT acc_uniform_password_sys_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_acc_uniform_password_sys_rev FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Add new columns for system attribute mapping for password filter
ALTER TABLE sys_system_attribute_mapping ADD password_filter boolean NOT NULL DEFAULT false;
ALTER TABLE sys_system_attribute_mapping ADD transformation_uid_script text NULL;
ALTER TABLE sys_system_attribute_mapping ADD echo_timeout integer NOT NULL DEFAULT 180;

ALTER TABLE sys_system_attribute_mapping_a ADD password_filter boolean;
ALTER TABLE sys_system_attribute_mapping_a ADD password_filter_m boolean;
ALTER TABLE sys_system_attribute_mapping_a ADD transformation_uid_script text;
ALTER TABLE sys_system_attribute_mapping_a ADD transformation_uid_script_m boolean;
ALTER TABLE sys_system_attribute_mapping_a ADD echo_timeout integer;
ALTER TABLE sys_system_attribute_mapping_a ADD echo_timeout_m boolean;
