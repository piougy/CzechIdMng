--
-- CzechIdM 7.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM (module core)


----- SEQUENCE hibernate_sequence -----
CREATE SEQUENCE hibernate_sequence
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 56
  CACHE 1;
  

----- TABLE idm_audit -----
CREATE TABLE idm_audit
(
  id bigint NOT NULL,
  changed_attributes character varying(255),
  entity_id bytea,
  modification character varying(255),
  modifier character varying(255),
  modifier_id bytea,
  original_modifier character varying(255),
  original_modifier_id bytea,
  realm_id bytea,
  "timestamp" bigint NOT NULL,
  type character varying(255),
  CONSTRAINT idm_audit_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_audit_changed_attributes
  ON idm_audit
  USING btree
  (changed_attributes);

CREATE INDEX idx_idm_audit_entity_id
  ON idm_audit
  USING btree
  (entity_id);

CREATE INDEX idx_idm_audit_modification
  ON idm_audit
  USING btree
  (modification);

CREATE INDEX idx_idm_audit_modifier
  ON idm_audit
  USING btree
  (modifier);

CREATE INDEX idx_idm_audit_original_modifier
  ON idm_audit
  USING btree
  (original_modifier);

CREATE INDEX idx_idm_audit_timestamp
  ON idm_audit
  USING btree
  ("timestamp");



----- TABLE idm_concept_role_request -----
CREATE TABLE idm_concept_role_request
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
  log text,
  operation character varying(255),
  state character varying(255) NOT NULL,
  valid_from date,
  valid_till date,
  wf_process_id character varying(255),
  identity_contract_id bytea,
  identity_role_id bytea,
  role_id bytea,
  request_role_id bytea NOT NULL,
  role_tree_node_id bytea,
  CONSTRAINT idm_concept_role_request_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_conc_role_ident_c
  ON idm_concept_role_request
  USING btree
  (identity_contract_id);

CREATE INDEX idx_idm_conc_role_request
  ON idm_concept_role_request
  USING btree
  (request_role_id);

CREATE INDEX idx_idm_conc_role_role
  ON idm_concept_role_request
  USING btree
  (role_id);

  
----- TABLE idm_confidential_storage -----
CREATE TABLE idm_confidential_storage
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
  storage_key character varying(255) NOT NULL,
  owner_id bytea NOT NULL,
  owner_type character varying(255) NOT NULL,
  storage_value bytea,
  CONSTRAINT idm_confidential_storage_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_confidential_storage_key
  ON idm_confidential_storage
  USING btree
  (storage_key);

CREATE INDEX idx_confidential_storage_o_i
  ON idm_confidential_storage
  USING btree
  (owner_id);

CREATE INDEX idx_confidential_storage_o_t
  ON idm_confidential_storage
  USING btree
  (owner_type);


----- TABLE idm_configuration -----
CREATE TABLE idm_configuration
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
  confidential boolean NOT NULL,
  name character varying(255) NOT NULL,
  secured boolean NOT NULL,
  value character varying(255),
  CONSTRAINT idm_configuration_pkey PRIMARY KEY (id),
  CONSTRAINT ux_configuration_name UNIQUE (name)
);

  
----- TABLE idm_forest_index -----
CREATE TABLE idm_forest_index
(
  id bigint NOT NULL,
  forest_tree_type character varying(255) NOT NULL,
  lft bigint,
  rgt bigint,
  content_id bytea,
  parent_id bigint,
  CONSTRAINT idm_forest_index_pkey PRIMARY KEY (id),
  CONSTRAINT ux_forest_index_content UNIQUE (content_id)
);

CREATE INDEX idx_forest_index_content
  ON idm_forest_index
  USING btree
  (content_id);

CREATE INDEX idx_forest_index_lft
  ON idm_forest_index
  USING btree
  (lft);

CREATE INDEX idx_forest_index_parent
  ON idm_forest_index
  USING btree
  (parent_id);

CREATE INDEX idx_forest_index_rgt
  ON idm_forest_index
  USING btree
  (rgt);

CREATE INDEX idx_forest_index_tree_type
  ON idm_forest_index
  USING btree
  (forest_tree_type);


----- TABLE idm_form_attribute -----
CREATE TABLE idm_form_attribute
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
  confidential boolean NOT NULL,
  default_value text,
  description character varying(2000),
  display_name character varying(255) NOT NULL,
  multiple boolean NOT NULL,
  name character varying(255) NOT NULL,
  persistent_type character varying(45) NOT NULL,
  placeholder character varying(255),
  readonly boolean NOT NULL,
  required boolean NOT NULL,
  seq smallint,
  unmodifiable boolean NOT NULL,
  definition_id bytea NOT NULL,
  CONSTRAINT idm_form_attribute_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_f_a_definition_name UNIQUE (definition_id, name),
  CONSTRAINT idm_form_attribute_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_idm_f_a_definition_def
  ON idm_form_attribute
  USING btree
  (definition_id);

  
----- TABLE idm_form_definition -----
CREATE TABLE idm_form_definition
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
  name character varying(255) NOT NULL,
  definition_type character varying(255) NOT NULL,
  unmodifiable boolean NOT NULL,
  CONSTRAINT idm_form_definition_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_form_definition_tn UNIQUE (definition_type, name)
);

  
----- TABLE idm_i_contract_form_value -----
CREATE TABLE idm_i_contract_form_value
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
  CONSTRAINT idm_i_contract_form_value_pkey PRIMARY KEY (id),
  CONSTRAINT idm_i_contract_form_value_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_idm_i_contract_form_a
  ON idm_i_contract_form_value
  USING btree
  (owner_id);

CREATE INDEX idx_idm_i_contract_form_a_def
  ON idm_i_contract_form_value
  USING btree
  (attribute_id);

CREATE INDEX idx_idm_i_contract_form_a_str
  ON idm_i_contract_form_value
  USING btree
  (string_value);


----- TABLE idm_identity -----
CREATE TABLE idm_identity
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
  description character varying(2000),
  disabled boolean NOT NULL,
  email character varying(255),
  first_name character varying(255),
  last_name character varying(255) NOT NULL,
  phone character varying(30),
  title_after character varying(100),
  title_before character varying(100),
  username character varying(255) NOT NULL,
  version bigint,
  CONSTRAINT idm_identity_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_identity_username UNIQUE (username)
);


----- TABLE idm_identity_contract -----
CREATE TABLE idm_identity_contract
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
  description character varying(2000),
  disabled boolean NOT NULL,
  externe boolean NOT NULL,
  main boolean NOT NULL,
  "position" character varying(255),
  valid_from date,
  valid_till date,
  guarantee_id bytea,
  identity_id bytea NOT NULL,
  work_position_id bytea,
  CONSTRAINT idm_identity_contract_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_identity_contract_gnt
  ON idm_identity_contract
  USING btree
  (guarantee_id);

CREATE INDEX idx_idm_identity_contract_idnt
  ON idm_identity_contract
  USING btree
  (identity_id);

CREATE INDEX idx_idm_identity_contract_wp
  ON idm_identity_contract
  USING btree
  (work_position_id);


----- TABLE idm_identity_form_value -----
CREATE TABLE idm_identity_form_value
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
  CONSTRAINT idm_identity_form_value_pkey PRIMARY KEY (id),
  CONSTRAINT idm_identity_form_value_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_idm_identity_form_a
  ON idm_identity_form_value
  USING btree
  (owner_id);

CREATE INDEX idx_idm_identity_form_a_def
  ON idm_identity_form_value
  USING btree
  (attribute_id);

CREATE INDEX idx_idm_identity_form_a_str
  ON idm_identity_form_value
  USING btree
  (string_value);


----- TABLE idm_identity_role -----
CREATE TABLE idm_identity_role
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
  valid_from date,
  valid_till date,
  identity_contract_id bytea NOT NULL,
  role_id bytea NOT NULL,
  role_tree_node_id bytea,
  CONSTRAINT idm_identity_role_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_identity_role_aut_r
  ON idm_identity_role
  USING btree
  (role_tree_node_id);

CREATE INDEX idx_idm_identity_role_ident_c
  ON idm_identity_role
  USING btree
  (identity_contract_id);

CREATE INDEX idx_idm_identity_role_role
  ON idm_identity_role
  USING btree
  (role_id);


----- TABLE idm_long_running_task -----
CREATE TABLE idm_long_running_task
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
  task_count bigint,
  task_counter bigint,
  instance_id character varying(255) NOT NULL,
  result_cause text,
  result_code character varying(255),
  result_model bytea,
  result_state character varying(45) NOT NULL,
  running boolean NOT NULL,
  task_description character varying(255),
  task_properties bytea,
  task_type character varying(255) NOT NULL,
  thread_id bigint NOT NULL,
  thread_name character varying(255),
  CONSTRAINT idm_long_running_task_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_long_r_t_inst
  ON idm_long_running_task
  USING btree
  (instance_id);

CREATE INDEX idx_idm_long_r_t_type
  ON idm_long_running_task
  USING btree
  (task_type);


----- TABLE idm_notification -----
CREATE TABLE idm_notification
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
  html_message text,
  level character varying(45) NOT NULL,
  result_model bytea,
  subject character varying(255),
  text_message text,
  sent timestamp without time zone,
  sent_log character varying(2000),
  topic character varying(255),
  identity_sender_id bytea,
  notification_template_id bytea,
  parent_notification_id bytea,
  CONSTRAINT idm_notification_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_notification_parent
  ON idm_notification
  USING btree
  (parent_notification_id);

CREATE INDEX idx_idm_notification_sender
  ON idm_notification
  USING btree
  (identity_sender_id);


----- TABLE idm_notification_configuration -----
CREATE TABLE idm_notification_configuration
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
  description character varying(2000),
  level character varying(45),
  notification_type character varying(255) NOT NULL,
  topic character varying(255) NOT NULL,
  template_id bytea,
  CONSTRAINT idm_notification_configuration_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_not_conf UNIQUE (topic, level, notification_type)
);

CREATE INDEX idx_idm_not_conf_level
  ON idm_notification_configuration
  USING btree
  (level);

CREATE INDEX idx_idm_not_conf_topic
  ON idm_notification_configuration
  USING btree
  (topic);

CREATE INDEX idx_idm_not_conf_type
  ON idm_notification_configuration
  USING btree
  (notification_type);

CREATE INDEX idx_idm_not_template
  ON idm_notification_configuration
  USING btree
  (template_id);


----- TABLE idm_notification_console -----
CREATE TABLE idm_notification_console
(
  id bytea NOT NULL,
  CONSTRAINT idm_notification_console_pkey PRIMARY KEY (id)
);


----- TABLE idm_notification_email -----
CREATE TABLE idm_notification_email
(
  id bytea NOT NULL,
  CONSTRAINT idm_notification_email_pkey PRIMARY KEY (id)
);


----- TABLE idm_notification_log -----
CREATE TABLE idm_notification_log
(
  id bytea NOT NULL,
  CONSTRAINT idm_notification_log_pkey PRIMARY KEY (id)
);


----- TABLE idm_notification_recipient -----
CREATE TABLE idm_notification_recipient
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
  real_recipient character varying(255),
  identity_recipient_id bytea,
  notification_id bytea NOT NULL,
  CONSTRAINT idm_notification_recipient_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_notification_rec_idnt
  ON idm_notification_recipient
  USING btree
  (identity_recipient_id);

CREATE INDEX idx_idm_notification_rec_not
  ON idm_notification_recipient
  USING btree
  (notification_id);


----- TABLE idm_notification_template -----
CREATE TABLE idm_notification_template
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
  body_html text,
  body_text text,
  code character varying(255) NOT NULL,
  module character varying(255),
  name character varying(255) NOT NULL,
  parameter character varying(255),
  subject character varying(255) NOT NULL,
  unmodifiable boolean NOT NULL,
  CONSTRAINT idm_notification_template_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_notification_template_code UNIQUE (code),
  CONSTRAINT ux_idm_notification_template_name UNIQUE (name)
);


----- TABLE idm_notification_websocket -----
CREATE TABLE idm_notification_websocket
(
  id bytea NOT NULL,
  CONSTRAINT idm_notification_websocket_pkey PRIMARY KEY (id)
);


----- TABLE idm_password -----
CREATE TABLE idm_password
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
  must_change boolean,
  password character varying(255),
  valid_from date,
  valid_till date,
  identity_id bytea NOT NULL,
  CONSTRAINT idm_password_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_password_identity UNIQUE (identity_id)
);

CREATE INDEX idx_idm_password_identity
  ON idm_password
  USING btree
  (identity_id);


----- TABLE idm_password_policy -----
CREATE TABLE idm_password_policy
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
  default_policy boolean NOT NULL,
  description character varying(2000),
  disabled boolean NOT NULL,
  enchanced_control boolean NOT NULL,
  generate_type character varying(255),
  identity_attribute_check character varying(255),
  lower_char_base character varying(255),
  lower_char_required boolean NOT NULL,
  max_history_similar integer,
  max_password_age integer,
  max_password_length integer,
  min_lower_char integer,
  min_number integer,
  min_password_age integer,
  min_password_length integer,
  min_rules_to_fulfill integer,
  min_special_char integer,
  min_upper_char integer,
  name character varying(255) NOT NULL,
  number_base character varying(255),
  number_required boolean NOT NULL,
  passphrase_words integer,
  password_length_required boolean NOT NULL,
  prohibited_characters character varying(255),
  special_char_base character varying(255),
  special_char_required boolean NOT NULL,
  type character varying(255),
  upper_char_base character varying(255),
  upper_char_required boolean NOT NULL,
  weak_pass character varying(255),
  weak_pass_required boolean NOT NULL,
  CONSTRAINT idm_password_policy_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_pass_policy_name UNIQUE (name)
);


----- TABLE idm_role -----
CREATE TABLE idm_role
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
  approve_add_workflow character varying(255),
  approve_remove_workflow character varying(255),
  description character varying(2000),
  disabled boolean NOT NULL,
  name character varying(255) NOT NULL,
  priority integer NOT NULL,
  role_type character varying(255) NOT NULL,
  version bigint,
  CONSTRAINT idm_role_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_role_name UNIQUE (name)
);


----- TABLE idm_role_authority -----
CREATE TABLE idm_role_authority
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
  action_permission character varying(255) NOT NULL,
  target_permission character varying(255) NOT NULL,
  role_id bytea NOT NULL,
  CONSTRAINT idm_role_authority_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_role_authority_role
  ON idm_role_authority
  USING btree
  (role_id);


----- TABLE idm_role_catalogue -----
CREATE TABLE idm_role_catalogue
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
  code character varying(255) NOT NULL,
  description character varying(2000),
  name character varying(255) NOT NULL,
  url character varying(255),
  url_title character varying(255),
  parent_id bytea,
  CONSTRAINT idm_role_catalogue_pkey PRIMARY KEY (id),
  CONSTRAINT ux_role_catalogue_code UNIQUE (code)
);

CREATE INDEX idx_idm_role_cat_parent
  ON idm_role_catalogue
  USING btree
  (parent_id);

CREATE INDEX ux_role_catalogue_name
  ON idm_role_catalogue
  USING btree
  (name);


----- TABLE idm_role_catalogue_role -----
CREATE TABLE idm_role_catalogue_role
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
  role_catalogue_id bytea NOT NULL,
  CONSTRAINT idm_role_catalogue_role_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_role_catalogue_id
  ON idm_role_catalogue_role
  USING btree
  (role_catalogue_id);

CREATE INDEX idx_idm_role_id
  ON idm_role_catalogue_role
  USING btree
  (role_id);


----- TABLE idm_role_composition -----
CREATE TABLE idm_role_composition
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
  sub_id bytea NOT NULL,
  superior_id bytea NOT NULL,
  CONSTRAINT idm_role_composition_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_role_composition_sub
  ON idm_role_composition
  USING btree
  (sub_id);

CREATE INDEX idx_idm_role_composition_super
  ON idm_role_composition
  USING btree
  (superior_id);


----- TABLE idm_role_form_value -----
CREATE TABLE idm_role_form_value
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
  CONSTRAINT idm_role_form_value_pkey PRIMARY KEY (id),
  CONSTRAINT idm_role_form_value_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_idm_role_form_a
  ON idm_role_form_value
  USING btree
  (owner_id);

CREATE INDEX idx_idm_role_form_a_def
  ON idm_role_form_value
  USING btree
  (attribute_id);

CREATE INDEX idx_idm_role_form_a_str
  ON idm_role_form_value
  USING btree
  (string_value);


----- TABLE idm_role_guarantee -----
CREATE TABLE idm_role_guarantee
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
  guarantee_id bytea NOT NULL,
  role_id bytea NOT NULL,
  CONSTRAINT idm_role_guarantee_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_role_guarantee_gnt
  ON idm_role_guarantee
  USING btree
  (guarantee_id);

CREATE INDEX idx_idm_role_guarantee_role
  ON idm_role_guarantee
  USING btree
  (role_id);


----- TABLE idm_role_request -----
CREATE TABLE idm_role_request
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
  description character varying(255),
  execute_immediately boolean NOT NULL,
  log text,
  original_request text,
  requested_by_type character varying(255) NOT NULL,
  state character varying(255) NOT NULL,
  wf_process_id character varying(255),
  applicant_id bytea NOT NULL,
  duplicated_to_request bytea,
  CONSTRAINT idm_role_request_pkey PRIMARY KEY (id)
);


----- TABLE idm_role_tree_node -----
CREATE TABLE idm_role_tree_node
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
  recursion_type character varying(255) NOT NULL,
  role_id bytea NOT NULL,
  tree_node_id bytea NOT NULL,
  CONSTRAINT idm_role_tree_node_pkey PRIMARY KEY (id),
  CONSTRAINT ux_idm_role_tree_node UNIQUE (role_id, tree_node_id, recursion_type)
);

CREATE INDEX idx_idm_role_tree_node
  ON idm_role_tree_node
  USING btree
  (tree_node_id);

CREATE INDEX idx_idm_role_tree_role
  ON idm_role_tree_node
  USING btree
  (role_id);

  
----- TABLE idm_script -----
CREATE TABLE idm_script
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
  category character varying(255) NOT NULL,
  description character varying(2000),
  name character varying(255) NOT NULL,
  script text,
  CONSTRAINT idm_script_pkey PRIMARY KEY (id),
  CONSTRAINT ux_script_name UNIQUE (name)
);

CREATE INDEX ux_script_category
  ON idm_script
  USING btree
  (category);


----- TABLE idm_tree_node -----
CREATE TABLE idm_tree_node
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
  code character varying(255) NOT NULL,
  disabled boolean NOT NULL,
  name character varying(255) NOT NULL,
  version bigint,
  parent_id bytea,
  tree_type_id bytea NOT NULL,
  CONSTRAINT idm_tree_node_pkey PRIMARY KEY (id),
  CONSTRAINT ux_tree_node_code UNIQUE (tree_type_id, code)
);

CREATE INDEX idx_idm_tree_node_parent
  ON idm_tree_node
  USING btree
  (parent_id);

CREATE INDEX idx_idm_tree_node_type
  ON idm_tree_node
  USING btree
  (tree_type_id);


----- TABLE idm_tree_node_form_value -----
CREATE TABLE idm_tree_node_form_value
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
  CONSTRAINT idm_tree_node_form_value_pkey PRIMARY KEY (id),
  CONSTRAINT idm_tree_node_form_value_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_idm_tree_node_form_a
  ON idm_tree_node_form_value
  USING btree
  (owner_id);

CREATE INDEX idx_idm_tree_node_form_a_def
  ON idm_tree_node_form_value
  USING btree
  (attribute_id);

CREATE INDEX idx_idm_tree_node_form_a_str
  ON idm_tree_node_form_value
  USING btree
  (string_value);


----- TABLE idm_tree_type -----
CREATE TABLE idm_tree_type
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
  code character varying(255) NOT NULL,
  default_tree_type boolean NOT NULL,
  name character varying(255) NOT NULL,
  default_tree_node_id bytea,
  CONSTRAINT idm_tree_type_pkey PRIMARY KEY (id),
  CONSTRAINT ux_tree_type_code UNIQUE (code)
);

CREATE INDEX ux_tree_type_name
  ON idm_tree_type
  USING btree
  (name);
