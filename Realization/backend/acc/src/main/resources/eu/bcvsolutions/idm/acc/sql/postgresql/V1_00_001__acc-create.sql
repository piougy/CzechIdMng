--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM 7.0 - Module Acc


----- TABLE acc_account -----
CREATE TABLE acc_account
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
  account_type character varying(255) NOT NULL,
  uid character varying(1000) NOT NULL,
  system_id bytea NOT NULL,
  system_entity_id bytea,
  CONSTRAINT acc_account_pkey PRIMARY KEY (id),
  CONSTRAINT ux_acc_account_sys_entity UNIQUE (system_entity_id),
  CONSTRAINT ux_account_uid UNIQUE (uid, system_id)
);

CREATE INDEX idx_acc_account_sys_entity
  ON acc_account
  USING btree
  (system_entity_id);

CREATE INDEX idx_acc_account_sys_id
  ON acc_account
  USING btree
  (system_id);


----- TABLE acc_identity_account -----
CREATE TABLE acc_identity_account
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
  ownership boolean NOT NULL,
  account_id bytea NOT NULL,
  identity_id bytea NOT NULL,
  identity_role_id bytea,
  role_system_id bytea,
  CONSTRAINT acc_identity_account_pkey PRIMARY KEY (id),
  CONSTRAINT ux_identity_account UNIQUE (identity_id, account_id, role_system_id, identity_role_id)
);

CREATE INDEX idx_acc_identity_account_acc
  ON acc_identity_account
  USING btree
  (account_id);

CREATE INDEX idx_acc_identity_account_ident
  ON acc_identity_account
  USING btree
  (identity_id);

CREATE INDEX idx_acc_identity_identity_role
  ON acc_identity_account
  USING btree
  (identity_role_id);


----- TABLE sys_provisioning_archive -----
CREATE TABLE sys_provisioning_archive
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
  entity_identifier bytea,
  entity_type character varying(255) NOT NULL,
  operation_type character varying(255) NOT NULL,
  provisioning_context bytea NOT NULL,
  result_cause text,
  result_code character varying(255),
  result_model bytea,
  result_state character varying(45) NOT NULL,
  system_entity_uid character varying(255),
  system_id bytea NOT NULL,
  CONSTRAINT sys_provisioning_archive_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_sys_p_o_arch_created
  ON sys_provisioning_archive
  USING btree
  (created);

CREATE INDEX idx_sys_p_o_arch_entity_identifier
  ON sys_provisioning_archive
  USING btree
  (entity_identifier);

CREATE INDEX idx_sys_p_o_arch_entity_type
  ON sys_provisioning_archive
  USING btree
  (entity_type);

CREATE INDEX idx_sys_p_o_arch_operation_type
  ON sys_provisioning_archive
  USING btree
  (operation_type);

CREATE INDEX idx_sys_p_o_arch_system
  ON sys_provisioning_archive
  USING btree
  (system_id);

CREATE INDEX idx_sys_p_o_arch_uid
  ON sys_provisioning_archive
  USING btree
  (system_entity_uid);


----- TABLE sys_provisioning_batch -----
CREATE TABLE sys_provisioning_batch
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
  next_attempt timestamp without time zone,
  CONSTRAINT sys_provisioning_batch_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_sys_p_b_next
  ON sys_provisioning_batch
  USING btree
  (next_attempt);


----- TABLE sys_provisioning_operation -----
CREATE TABLE sys_provisioning_operation
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
  entity_identifier bytea,
  operation_type character varying(255) NOT NULL,
  provisioning_context bytea NOT NULL,
  system_entity_id bytea NOT NULL,
  CONSTRAINT sys_provisioning_operation_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_sys_p_o_created
  ON sys_provisioning_operation
  USING btree
  (created);

CREATE INDEX idx_sys_p_o_entity_identifier
  ON sys_provisioning_operation
  USING btree
  (entity_identifier);

CREATE INDEX idx_sys_p_o_entity_sys_e_id
  ON sys_provisioning_operation
  USING btree
  (system_entity_id);

CREATE INDEX idx_sys_p_o_operation_type
  ON sys_provisioning_operation
  USING btree
  (operation_type);


----- TABLE sys_provisioning_request -----
CREATE TABLE sys_provisioning_request
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
  max_attempts integer,
  result_cause text,
  result_code character varying(255),
  result_model bytea,
  result_state character varying(45) NOT NULL,
  provisioning_batch_id bytea,
  provisioning_operation_id bytea NOT NULL,
  CONSTRAINT sys_provisioning_request_pkey PRIMARY KEY (id),
  CONSTRAINT ux_sys_prov_req_operation_id UNIQUE (provisioning_operation_id)
);

  
----- TABLE sys_role_system -----
CREATE TABLE sys_role_system
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
  role_id bytea NOT NULL,
  system_id bytea NOT NULL,
  system_mapping_id bytea NOT NULL,
  CONSTRAINT sys_role_system_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_sys_role_system_role_id
  ON sys_role_system
  USING btree
  (role_id);

CREATE INDEX idx_sys_role_system_system_id
  ON sys_role_system
  USING btree
  (system_id);


----- TABLE sys_role_system_attribute -----
CREATE TABLE sys_role_system_attribute
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
  confidential_attribute boolean NOT NULL,
  disabled_default_attribute boolean NOT NULL,
  entity_attribute boolean NOT NULL,
  extended_attribute boolean NOT NULL,
  idm_property_name character varying(255),
  name character varying(255),
  send_always boolean NOT NULL,
  send_only_if_not_null boolean NOT NULL,
  strategy_type character varying(255) NOT NULL,
  transform_script text,
  uid boolean NOT NULL,
  role_system_id bytea NOT NULL,
  system_attr_mapping_id bytea NOT NULL,
  CONSTRAINT sys_role_system_attribute_pkey PRIMARY KEY (id),
  CONSTRAINT ux_role_sys_atth_name UNIQUE (name, role_system_id),
  CONSTRAINT ux_role_sys_atth_pname UNIQUE (idm_property_name, role_system_id)
);


----- TABLE sys_schema_attribute -----
CREATE TABLE sys_schema_attribute
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
  class_type character varying(255) NOT NULL,
  createable boolean NOT NULL,
  multivalued boolean NOT NULL,
  name character varying(255) NOT NULL,
  native_name character varying(255),
  readable boolean NOT NULL,
  required boolean NOT NULL,
  returned_by_default boolean NOT NULL,
  updateable boolean NOT NULL,
  object_class_id bytea NOT NULL,
  CONSTRAINT sys_schema_attribute_pkey PRIMARY KEY (id),
  CONSTRAINT ux_schema_att_name_objclass UNIQUE (name, object_class_id)
);


----- TABLE sys_schema_obj_class -----
CREATE TABLE sys_schema_obj_class
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
  auxiliary boolean NOT NULL,
  container boolean NOT NULL,
  object_class_name character varying(255) NOT NULL,
  system_id bytea NOT NULL,
  CONSTRAINT sys_schema_obj_class_pkey PRIMARY KEY (id),
  CONSTRAINT ux_schema_class_name_sys UNIQUE (object_class_name, system_id)
);


----- TABLE sys_sync_action_log -----
CREATE TABLE sys_sync_action_log
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
  operation_count integer NOT NULL,
  result character varying(255) NOT NULL,
  sync_action character varying(255) NOT NULL,
  sync_log_id bytea NOT NULL,
  CONSTRAINT sys_sync_action_log_pkey PRIMARY KEY (id)
);


----- TABLE sys_sync_config -----
CREATE TABLE sys_sync_config
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
  custom_filter boolean NOT NULL,
  custom_filter_script text,
  description character varying(2000),
  enabled boolean NOT NULL,
  filter_operation character varying(255) NOT NULL,
  linked_action character varying(255) NOT NULL,
  linked_action_wf character varying(255),
  missing_account_action character varying(255) NOT NULL,
  missing_account_action_wf character varying(255),
  missing_entity_action character varying(255) NOT NULL,
  missing_entity_action_wf character varying(255),
  name character varying(255) NOT NULL,
  reconciliation boolean NOT NULL,
  token text,
  unlinked_action character varying(255) NOT NULL,
  unlinked_action_wf character varying(255),
  correlation_attribute_id bytea NOT NULL,
  filter_attribute_id bytea,
  system_mapping_id bytea NOT NULL,
  token_attribute_id bytea,
  CONSTRAINT sys_sync_config_pkey PRIMARY KEY (id),
  CONSTRAINT ux_sys_s_config_name UNIQUE (name, system_mapping_id)
);

CREATE INDEX idx_sys_s_config_correl
  ON sys_sync_config
  USING btree
  (correlation_attribute_id);

CREATE INDEX idx_sys_s_config_filter
  ON sys_sync_config
  USING btree
  (filter_attribute_id);

CREATE INDEX idx_sys_s_config_mapping
  ON sys_sync_config
  USING btree
  (system_mapping_id);

CREATE INDEX idx_sys_s_config_token
  ON sys_sync_config
  USING btree
  (token_attribute_id);

  
----- TABLE sys_sync_item_log -----
CREATE TABLE sys_sync_item_log
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
  display_name character varying(255),
  identification character varying(255),
  log text,
  message character varying(2000),
  type character varying(255),
  sync_action_log_id bytea NOT NULL,
  CONSTRAINT sys_sync_item_log_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_sys_s_i_l_action
  ON sys_sync_item_log
  USING btree
  (sync_action_log_id);

  
----- TABLE sys_sync_log -----
CREATE TABLE sys_sync_log
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
  contains_error boolean NOT NULL,
  ended timestamp without time zone,
  log text,
  running boolean NOT NULL,
  started timestamp without time zone,
  token text,
  synchronization_config_id bytea NOT NULL,
  CONSTRAINT sys_sync_log_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_sys_s_l_config
  ON sys_sync_log
  USING btree
  (synchronization_config_id);

  
----- TABLE sys_system -----
CREATE TABLE sys_system
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
  connector_bundle_name character varying(255),
  connector_bundle_version character varying(30),
  connector_name character varying(255),
  connector_framework character varying(255),
  host character varying(255),
  port integer,
  timeout integer,
  use_ssl boolean NOT NULL,
  description character varying(2000),
  disabled boolean NOT NULL,
  name character varying(255) NOT NULL,
  queue boolean NOT NULL,
  readonly boolean NOT NULL,
  remote boolean NOT NULL,
  version bigint,
  virtual boolean NOT NULL,
  password_pol_gen_id bytea,
  password_pol_val_id bytea,
  CONSTRAINT sys_system_pkey PRIMARY KEY (id),
  CONSTRAINT ux_system_name UNIQUE (name)
);

CREATE INDEX idx_idm_password_pol_gen
  ON sys_system
  USING btree
  (password_pol_val_id);

CREATE INDEX idx_idm_password_pol_val
  ON sys_system
  USING btree
  (password_pol_gen_id);


----- TABLE sys_system_attribute_mapping -----
CREATE TABLE sys_system_attribute_mapping
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
  authentication_attribute boolean NOT NULL,
  confidential_attribute boolean NOT NULL,
  disabled_attribute boolean NOT NULL,
  entity_attribute boolean NOT NULL,
  extended_attribute boolean NOT NULL,
  idm_property_name character varying(255),
  name character varying(255) NOT NULL,
  send_always boolean NOT NULL,
  send_only_if_not_null boolean NOT NULL,
  strategy_type character varying(255) NOT NULL,
  transform_from_res_script text,
  transform_to_res_script text,
  uid boolean NOT NULL,
  schema_attribute_id bytea NOT NULL,
  system_mapping_id bytea NOT NULL,
  CONSTRAINT sys_system_attribute_mapping_pkey PRIMARY KEY (id),
  CONSTRAINT ux_sys_attr_m_attr UNIQUE (system_mapping_id, schema_attribute_id, strategy_type),
  CONSTRAINT ux_sys_attr_m_name_enth UNIQUE (name, system_mapping_id)
);


----- TABLE sys_system_entity -----
CREATE TABLE sys_system_entity
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
  entity_type character varying(255) NOT NULL,
  uid character varying(1000) NOT NULL,
  wish boolean NOT NULL,
  system_id bytea NOT NULL,
  CONSTRAINT sys_system_entity_pkey PRIMARY KEY (id),
  CONSTRAINT ux_system_entity_type_uid UNIQUE (entity_type, uid, system_id)
);

CREATE INDEX idx_sys_system_entity_system
  ON sys_system_entity
  USING btree
  (system_id);


CREATE INDEX idx_sys_system_entity_type
  ON sys_system_entity
  USING btree
  (entity_type);

  
CREATE INDEX idx_sys_system_entity_uid
  ON sys_system_entity
  USING btree
  (uid);

  
----- TABLE sys_system_form_value -----
CREATE TABLE sys_system_form_value
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
  boolean_value boolean,
  byte_value bytea,
  confidential boolean NOT NULL,
  date_value timestamp without time zone,
  double_value numeric(38,4),
  long_value bigint,
  persistent_type character varying(45) NOT NULL,
  seq smallint,
  string_value text,
  attribute_id bytea NOT NULL,
  owner_id bytea NOT NULL,
  CONSTRAINT sys_system_form_value_pkey PRIMARY KEY (id),
  CONSTRAINT sys_system_form_value_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_sys_sys_form_a
  ON sys_system_form_value
  USING btree
  (owner_id);

CREATE INDEX idx_sys_sys_form_a_def
  ON sys_system_form_value
  USING btree
  (attribute_id);


----- TABLE sys_system_mapping -----
CREATE TABLE sys_system_mapping
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
  entity_type character varying(255) NOT NULL,
  name character varying(255) NOT NULL,
  operation_type character varying(255) NOT NULL,
  object_class_id bytea NOT NULL,
  CONSTRAINT sys_system_mapping_pkey PRIMARY KEY (id),
  CONSTRAINT ux_sys_s_mapping_name UNIQUE (name, object_class_id)
);

CREATE INDEX idx_sys_s_mapping_e_type
  ON sys_system_mapping
  USING btree
  (entity_type);

CREATE INDEX idx_sys_s_mapping_o_c_id
  ON sys_system_mapping
  USING btree
  (object_class_id);

CREATE INDEX idx_sys_s_mapping_o_type
  ON sys_system_mapping
  USING btree
  (operation_type);
