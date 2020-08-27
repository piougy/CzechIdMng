--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Uniform password definition and password filter definition for system atribute mapping

CREATE TABLE acc_uniform_password
(
  id binary(16) NOT NULL,
  created datetime2 NOT NULL,
  creator nvarchar(255) NOT NULL,
  creator_id binary(16),
  modified datetime2,
  modifier nvarchar(255),
  modifier_id binary(16),
  original_creator nvarchar(255),
  original_creator_id binary(16),
  original_modifier nvarchar(255),
  original_modifier_id binary(16),
  realm_id binary(16),
  transaction_id binary(16),
  code nvarchar(255),
  description nvarchar(2000),
  disabled bit NOT NULL,
  change_in_idm bit NOT NULL,
  CONSTRAINT acc_uniform_password_pkey PRIMARY KEY (id),
  CONSTRAINT ux_acc_uniform_password_code UNIQUE (code)
);

CREATE TABLE acc_uniform_password_a
(
  id binary(16) NOT NULL,
  rev numeric(19,0) NOT NULL,
  revtype numeric(19,0),
  created datetime2,
  created_m bit,
  creator nvarchar(255),
  creator_m bit,
  creator_id binary(16),
  creator_id_m bit,
  modified datetime2,
  modified_m bit,
  modifier nvarchar(255),
  modifier_m bit,
  modifier_id binary(16),
  modifier_id_m bit,
  original_creator nvarchar(255),
  original_creator_m bit,
  original_creator_id binary(16),
  original_creator_id_m bit,
  original_modifier nvarchar(255),
  original_modifier_m bit,
  original_modifier_id binary(16),
  original_modifier_id_m bit,
  realm_id binary(16),
  realm_id_m bit,
  transaction_id binary(16),
  transaction_id_m bit,
  code nvarchar(255),
  code_m bit,
  description nvarchar(2000),
  description_m bit,
  disabled bit,
  disabled_m bit,
  change_in_idm bit,
  change_in_idm_m bit,
  CONSTRAINT acc_uniform_password_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_acc_uniform_password_rev FOREIGN KEY (rev) REFERENCES idm_audit (id)
);

CREATE TABLE acc_uniform_password_system
(
  id binary(16) NOT NULL,
  created datetime2 NOT NULL,
  creator nvarchar(255) NOT NULL,
  creator_id binary(16),
  modified datetime2,
  modifier nvarchar(255),
  modifier_id binary(16),
  original_creator nvarchar(255),
  original_creator_id binary(16),
  original_modifier nvarchar(255),
  original_modifier_id binary(16),
  realm_id binary(16),
  transaction_id binary(16),
  system_id binary(16) NOT NULL,
  uniform_password_id binary(16) NOT NULL,
  CONSTRAINT acc_uniform_password_sys_pkey PRIMARY KEY (id),
  CONSTRAINT ux_acc_uniform_pass_id_sys_id UNIQUE (system_id,uniform_password_id)
);
 
CREATE INDEX idx_sys_system_id ON acc_uniform_password_system (system_id);

CREATE INDEX idx_acc_uniform_password_id ON acc_uniform_password_system (uniform_password_id);

CREATE TABLE acc_uniform_password_system_a
(
  id binary(16) NOT NULL,
  rev numeric(19,0) NOT NULL,
  revtype smallint,
  created datetime2,
  created_m bit,
  creator nvarchar(255),
  creator_m bit,
  creator_id binary(16),
  creator_id_m bit,
  modified datetime2,
  modified_m bit,
  modifier nvarchar(255),
  modifier_m bit,
  modifier_id binary(16),
  modifier_id_m bit,
  original_creator nvarchar(255),
  original_creator_m bit,
  original_creator_id binary(16),
  original_creator_id_m bit,
  original_modifier nvarchar(255),
  original_modifier_m bit,
  original_modifier_id binary(16),
  original_modifier_id_m bit,
  realm_id binary(16),
  realm_id_m bit,
  transaction_id binary(16),
  transaction_id_m bit,
  system_id binary(16),
  system_m bit,
  uniform_password_id binary(16),
  uniform_password_m bit,
  CONSTRAINT acc_uniform_password_sys_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_acc_uniform_password_sys_rev FOREIGN KEY (rev) REFERENCES idm_audit (id)
);

-- Add new columns for system attribute mapping for password filter
ALTER TABLE sys_system_attribute_mapping ADD password_filter bit NOT NULL;
ALTER TABLE sys_system_attribute_mapping ADD transformation_uid_script nvarchar(MAX) NULL;
ALTER TABLE sys_system_attribute_mapping ADD echo_timeout int NOT NULL DEFAULT 180;

ALTER TABLE sys_system_attribute_mapping_a ADD password_filter bit;
ALTER TABLE sys_system_attribute_mapping_a ADD password_filter_m bit;
ALTER TABLE sys_system_attribute_mapping_a ADD transformation_uid_script nvarchar(MAX);
ALTER TABLE sys_system_attribute_mapping_a ADD transformation_uid_script_m bit;
ALTER TABLE sys_system_attribute_mapping_a ADD echo_timeout int;
ALTER TABLE sys_system_attribute_mapping_a ADD echo_timeout_m bit;

