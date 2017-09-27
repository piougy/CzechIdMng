--
-- CzechIdM 7.4 Flyway script 
-- BCV solutions s.r.o.
--
-- Provisioning break

CREATE TABLE sys_provisioning_break_config
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
  disable_limit integer NOT NULL,
  disabled boolean NOT NULL,
  operation_disabled boolean NOT NULL,
  operation_type character varying(255) NOT NULL,
  period bigint NOT NULL,
  warning_limit integer,
  email_template_disabled bytea NOT NULL,
  email_template_warning bytea NOT NULL,
  system_id bytea NOT NULL,
  CONSTRAINT sys_provisioning_break_config_pkey PRIMARY KEY (id)
);

CREATE TABLE sys_provisioning_break_recipient
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
  break_config_id bytea NOT NULL,
  identity_id bytea NOT NULL,
  role_id bytea NOT NULL,
  CONSTRAINT sys_provisioning_break_recipient_pkey PRIMARY KEY (id)
);

CREATE TABLE sys_provisioning_break_config_a
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
  disable_limit integer,
  disable_limit_m boolean,
  disabled boolean,
  disabled_m boolean,
  operation_disabled boolean,
  operation_disabled_m boolean,
  operation_type character varying(255),
  operation_type_m boolean,
  period bigint,
  period_m boolean,
  warning_limit integer,
  warning_limit_m boolean,
  email_template_disabled bytea,
  email_template_disabled_m boolean,
  email_template_warning bytea,
  email_template_warning_m boolean,
  system_id bytea,
  system_m boolean,
  CONSTRAINT sys_provisioning_break_config_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_m6smdd15x3drkpslil9jadh62 FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE sys_provisioning_break_recipient_a
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
  break_config_id bytea,
  break_config_m boolean,
  identity_id bytea,
  identity_m boolean,
  role_id bytea,
  role_m boolean,
  CONSTRAINT sys_provisioning_break_recipient_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_cyr1rkx363m9k74llltevx3rp FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE sys_system ADD COLUMN "create" BOOLEAN;

ALTER TABLE sys_system ADD COLUMN "update" BOOLEAN;

ALTER TABLE sys_system ADD COLUMN "delete" BOOLEAN;
