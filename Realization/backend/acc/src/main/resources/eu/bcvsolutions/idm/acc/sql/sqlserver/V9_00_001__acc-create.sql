--
-- CzechIdM 9.0 Flyway script
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM (module acc)

CREATE TABLE acc_account (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	account_type varchar(255) NOT NULL,
	end_of_protection datetime,
	entity_type varchar(255),
	in_protection bit,
	uid varchar(1000) NOT NULL,
	system_id binary(16) NOT NULL,
	system_entity_id binary(16),
	CONSTRAINT acc_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_account_sys_entity ON acc_account (system_entity_id);
CREATE INDEX idx_acc_account_sys_id ON acc_account (system_id);
CREATE UNIQUE INDEX ux_acc_account_sys_entity ON acc_account (system_entity_id);
CREATE UNIQUE INDEX ux_account_uid ON acc_account (uid,system_id);


CREATE TABLE acc_contract_account (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	ownership bit NOT NULL,
	account_id binary(16) NOT NULL,
	contract_id binary(16) NOT NULL,
	CONSTRAINT acc_contract_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_contr_acc_acc ON acc_contract_account (account_id);
CREATE INDEX idx_acc_contr_acc_contr ON acc_contract_account (contract_id);


CREATE TABLE acc_contract_slice_account (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	ownership bit NOT NULL,
	account_id binary(16) NOT NULL,
	contract_slice_id binary(16) NOT NULL,
	CONSTRAINT acc_contract_slice_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_contr_sli_acc ON acc_contract_slice_account (account_id);
CREATE INDEX idx_acc_contr_sli_contr ON acc_contract_slice_account (contract_slice_id);


CREATE TABLE acc_identity_account (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	ownership bit NOT NULL,
	account_id binary(16) NOT NULL,
	identity_id binary(16) NOT NULL,
	identity_role_id binary(16),
	role_system_id binary(16),
	CONSTRAINT acc_identity_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_identity_account_acc ON acc_identity_account (account_id);
CREATE INDEX idx_acc_identity_account_ident ON acc_identity_account (identity_id);
CREATE INDEX idx_acc_identity_identity_role ON acc_identity_account (identity_role_id);
CREATE UNIQUE INDEX ux_identity_account ON acc_identity_account (identity_id,account_id,role_system_id,identity_role_id);


CREATE TABLE acc_role_account (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	ownership bit NOT NULL,
	account_id binary(16) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT acc_role_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_role_account_acc ON acc_role_account (account_id);
CREATE INDEX idx_acc_role_account_role ON acc_role_account (role_id);


CREATE TABLE acc_role_catalogue_account (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	ownership bit NOT NULL,
	account_id binary(16) NOT NULL,
	role_catalogue_id binary(16) NOT NULL,
	role_system_id binary(16),
	CONSTRAINT acc_role_catalogue_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_cat_account_acc ON acc_role_catalogue_account (account_id);
CREATE INDEX idx_acc_cat_account_tree ON acc_role_catalogue_account (role_catalogue_id);


CREATE TABLE acc_tree_account (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	ownership bit NOT NULL,
	account_id binary(16) NOT NULL,
	role_system_id binary(16),
	tree_node_id binary(16) NOT NULL,
	CONSTRAINT acc_tree_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_tree_account_acc ON acc_tree_account (account_id);
CREATE INDEX idx_acc_tree_account_tree ON acc_tree_account (tree_node_id);


CREATE TABLE sys_provisioning_archive (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	entity_identifier binary(255),
	entity_type varchar(255) NOT NULL,
	operation_type varchar(255) NOT NULL,
	provisioning_context image NOT NULL,
	result_cause text,
	result_code varchar(255),
	result_model image,
	result_state varchar(45) NOT NULL,
	system_entity_uid varchar(255),
	system_id binary(16) NOT NULL,
	CONSTRAINT sys_provisioning_archive_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_p_o_arch_created ON sys_provisioning_archive (created);
CREATE INDEX idx_sys_p_o_arch_entity_identifier ON sys_provisioning_archive (entity_identifier);
CREATE INDEX idx_sys_p_o_arch_entity_type ON sys_provisioning_archive (entity_type);
CREATE INDEX idx_sys_p_o_arch_operation_type ON sys_provisioning_archive (operation_type);
CREATE INDEX idx_sys_p_o_arch_system ON sys_provisioning_archive (system_id);
CREATE INDEX idx_sys_p_o_arch_uid ON sys_provisioning_archive (system_entity_uid);


CREATE TABLE sys_provisioning_batch (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	next_attempt datetime,
	system_entity_id binary(16) NOT NULL,
	CONSTRAINT sys_provisioning_batch_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_p_b_next ON sys_provisioning_batch (next_attempt);
CREATE INDEX idx_sys_p_b_sys_entity ON sys_provisioning_batch (system_entity_id);


CREATE TABLE sys_provisioning_break_config (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	disable_limit int,
	disabled bit NOT NULL,
	operation_type varchar(255) NOT NULL,
	period numeric(19,0) NOT NULL,
	warning_limit int,
	disable_template_id binary(16),
	system_id binary(16) NOT NULL,
	warning_template_id binary(16),
	CONSTRAINT sys_provisioning_break_config_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_prov_br_config_oper_type ON sys_provisioning_break_config (operation_type);
CREATE INDEX idx_sys_prov_br_config_system_id ON sys_provisioning_break_config (system_id);


CREATE TABLE sys_provisioning_break_recipient (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	break_config_id binary(16) NOT NULL,
	identity_id binary(16),
	role_id binary(16),
	CONSTRAINT sys_provisioning_break_recipient_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_prov_br_break_id ON sys_provisioning_break_recipient (break_config_id);
CREATE INDEX idx_sys_prov_br_recip_identity_id ON sys_provisioning_break_recipient (identity_id);
CREATE INDEX idx_sys_prov_br_recip_role_id ON sys_provisioning_break_recipient (role_id);


CREATE TABLE sys_provisioning_operation (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	current_attempt int,
	entity_identifier binary(255),
	entity_type varchar(255) NOT NULL,
	max_attempts int,
	operation_type varchar(255) NOT NULL,
	provisioning_context image NOT NULL,
	result_cause text,
	result_code varchar(255),
	result_model image,
	result_state varchar(45) NOT NULL,
	provisioning_batch_id binary(16),
	system_id binary(16) NOT NULL,
	system_entity_id binary(16) NOT NULL,
	CONSTRAINT sys_provisioning_operation_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_p_o_created ON sys_provisioning_operation (created);
CREATE INDEX idx_sys_p_o_entity_identifier ON sys_provisioning_operation (entity_identifier);
CREATE INDEX idx_sys_p_o_entity_type ON sys_provisioning_operation (entity_type);
CREATE INDEX idx_sys_p_o_operation_type ON sys_provisioning_operation (operation_type);
CREATE INDEX idx_sys_p_o_sys_entity ON sys_provisioning_operation (system_entity_id);
CREATE INDEX idx_sys_p_o_system ON sys_provisioning_operation (system_id);
CREATE INDEX idx_sys_pro_oper_batch_id ON sys_provisioning_operation (provisioning_batch_id);


CREATE TABLE sys_role_system (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	forward_acm_enabled bit NOT NULL,
	role_id binary(16) NOT NULL,
	system_id binary(16) NOT NULL,
	system_mapping_id binary(16) NOT NULL,
	CONSTRAINT sys_role_system_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_role_system_role_id ON sys_role_system (role_id);
CREATE INDEX idx_sys_role_system_system_id ON sys_role_system (system_id);


CREATE TABLE sys_role_system_attribute (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	confidential_attribute bit NOT NULL,
	disabled_default_attribute bit NOT NULL,
	entity_attribute bit NOT NULL,
	extended_attribute bit NOT NULL,
	idm_property_name varchar(255),
	name varchar(255),
	send_always bit NOT NULL,
	send_only_if_not_null bit NOT NULL,
	strategy_type varchar(255) NOT NULL,
	transform_script text,
	uid bit NOT NULL,
	role_system_id binary(16) NOT NULL,
	system_attr_mapping_id binary(16) NOT NULL,
	CONSTRAINT sys_role_system_attribute_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_role_sys_atth_name ON sys_role_system_attribute (name,role_system_id);
CREATE UNIQUE INDEX ux_role_sys_atth_pname ON sys_role_system_attribute (idm_property_name,role_system_id);


CREATE TABLE sys_schema_attribute (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	class_type varchar(255) NOT NULL,
	createable bit NOT NULL,
	multivalued bit NOT NULL,
	name varchar(255) NOT NULL,
	native_name varchar(255),
	readable bit NOT NULL,
	required bit NOT NULL,
	returned_by_default bit NOT NULL,
	updateable bit NOT NULL,
	object_class_id binary(16) NOT NULL,
	CONSTRAINT sys_schema_attribute_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_schema_att_name_objclass ON sys_schema_attribute (name,object_class_id);


CREATE TABLE sys_schema_obj_class (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	auxiliary bit NOT NULL,
	container bit NOT NULL,
	object_class_name varchar(255) NOT NULL,
	system_id binary(16) NOT NULL,
	CONSTRAINT sys_schema_obj_class_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_schema_class_name_sys ON sys_schema_obj_class (object_class_name,system_id);


CREATE TABLE sys_sync_action_log (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	operation_count int NOT NULL,
	[result] varchar(255) NOT NULL,
	sync_action varchar(255) NOT NULL,
	sync_log_id binary(16) NOT NULL,
	CONSTRAINT sys_sync_action_log_pkey PRIMARY KEY (id)
);


CREATE TABLE sys_sync_config (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	custom_filter bit NOT NULL,
	custom_filter_script text,
	description varchar(2000),
	enabled bit NOT NULL,
	filter_operation varchar(255) NOT NULL,
	linked_action varchar(255) NOT NULL,
	linked_action_wf varchar(255),
	missing_account_action varchar(255) NOT NULL,
	missing_account_action_wf varchar(255),
	missing_entity_action varchar(255) NOT NULL,
	missing_entity_action_wf varchar(255),
	name varchar(255) NOT NULL,
	reconciliation bit NOT NULL,
	roots_filter_script text,
	token text,
	unlinked_action varchar(255) NOT NULL,
	unlinked_action_wf varchar(255),
	correlation_attribute_id binary(16) NOT NULL,
	filter_attribute_id binary(16),
	system_mapping_id binary(16) NOT NULL,
	token_attribute_id binary(16),
	CONSTRAINT sys_sync_config_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_s_config_correl ON sys_sync_config (correlation_attribute_id);
CREATE INDEX idx_sys_s_config_filter ON sys_sync_config (filter_attribute_id);
CREATE INDEX idx_sys_s_config_mapping ON sys_sync_config (system_mapping_id);
CREATE INDEX idx_sys_s_config_token ON sys_sync_config (token_attribute_id);
CREATE UNIQUE INDEX ux_sys_s_config_name ON sys_sync_config (name,system_mapping_id);


CREATE TABLE sys_sync_contract_config (
	start_auto_role_rec bit NOT NULL,
	start_hr_processes bit NOT NULL,
	id binary(16) NOT NULL,
	default_leader_id binary(16),
	default_tree_node_id binary(16),
	default_tree_type_id binary(16),
	CONSTRAINT sys_sync_contract_config_pkey PRIMARY KEY (id),
	CONSTRAINT fk_bc6q115e4e8r9dautiucnweyi FOREIGN KEY (id) REFERENCES sys_sync_config(id)
);
CREATE INDEX idx_sys_s_cont_conf_lead ON sys_sync_contract_config (default_leader_id);
CREATE INDEX idx_sys_s_cont_conf_node ON sys_sync_contract_config (default_tree_node_id);
CREATE INDEX idx_sys_s_cont_conf_tree ON sys_sync_contract_config (default_tree_type_id);


CREATE TABLE sys_sync_identity_config (
	create_default_contract bit NOT NULL,
	start_auto_role_rec bit NOT NULL,
	id binary(16) NOT NULL,
	default_role_id binary(16),
	CONSTRAINT sys_sync_identity_config_pkey PRIMARY KEY (id),
	CONSTRAINT fk_mf7itnp88831l71bl3tkgcqpo FOREIGN KEY (id) REFERENCES sys_sync_config(id)
);
CREATE INDEX idx_sys_s_iden_conf_role ON sys_sync_identity_config (default_role_id);


CREATE TABLE sys_sync_item_log (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	display_name varchar(255),
	identification varchar(255) NOT NULL,
	log text,
	message varchar(2000),
	[type] varchar(255),
	sync_action_log_id binary(16) NOT NULL,
	CONSTRAINT sys_sync_item_log_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_s_i_l_action ON sys_sync_item_log (sync_action_log_id);


CREATE TABLE sys_sync_log (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	contains_error bit NOT NULL,
	ended datetime,
	log text,
	running bit NOT NULL,
	started datetime,
	token text,
	synchronization_config_id binary(16) NOT NULL,
	CONSTRAINT sys_sync_log_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_s_l_config ON sys_sync_log (synchronization_config_id);


CREATE TABLE sys_system (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	create_operation bit NOT NULL,
	delete_operation bit NOT NULL,
	update_operation bit NOT NULL,
	connector_bundle_name varchar(255),
	connector_bundle_version varchar(30),
	connector_name varchar(255),
	connector_framework varchar(255),
	host varchar(255),
	port int,
	timeout int,
	use_ssl bit NOT NULL,
	description varchar(2000),
	disabled bit NOT NULL,
	name varchar(255) NOT NULL,
	queue bit NOT NULL,
	readonly bit NOT NULL,
	remote bit NOT NULL,
	version numeric(19,0),
	virtual bit NOT NULL,
	password_pol_gen_id binary(16),
	password_pol_val_id binary(16),
	CONSTRAINT sys_system_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_password_pol_gen ON sys_system (password_pol_val_id);
CREATE INDEX idx_idm_password_pol_val ON sys_system (password_pol_gen_id);
CREATE UNIQUE INDEX ux_system_name ON sys_system (name);


CREATE TABLE sys_system_attribute_mapping (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	authentication_attribute bit NOT NULL,
	attribute_cached bit NOT NULL,
	confidential_attribute bit NOT NULL,
	disabled_attribute bit NOT NULL,
	entity_attribute bit NOT NULL,
	extended_attribute bit NOT NULL,
	idm_property_name varchar(255),
	name varchar(255) NOT NULL,
	send_always bit NOT NULL,
	send_on_password_change bit NOT NULL,
	send_only_if_not_null bit NOT NULL,
	strategy_type varchar(255) NOT NULL,
	transform_from_res_script text,
	transform_to_res_script text,
	uid bit NOT NULL,
	schema_attribute_id binary(16) NOT NULL,
	system_mapping_id binary(16) NOT NULL,
	CONSTRAINT sys_system_attribute_mapping_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_sys_attr_m_name_enth ON sys_system_attribute_mapping (name,system_mapping_id);


CREATE TABLE sys_system_entity (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	entity_type varchar(255) NOT NULL,
	uid varchar(1000) NOT NULL,
	wish bit NOT NULL,
	system_id binary(16) NOT NULL,
	CONSTRAINT sys_system_entity_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_system_entity_system ON sys_system_entity (system_id);
CREATE INDEX idx_sys_system_entity_type ON sys_system_entity (entity_type);
CREATE INDEX idx_sys_system_entity_uid ON sys_system_entity (uid);
CREATE UNIQUE INDEX ux_system_entity_type_uid ON sys_system_entity (entity_type,uid,system_id);


CREATE TABLE sys_system_form_value (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	boolean_value bit,
	byte_value varbinary(255),
	confidential bit NOT NULL,
	date_value datetime,
	double_value numeric(38,4),
	long_value numeric(19,0),
	persistent_type varchar(45) NOT NULL,
	seq smallint,
	short_text_value varchar(2000),
	string_value text,
	uuid_value binary(16),
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT sys_system_form_value_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_sys_form_a ON sys_system_form_value (owner_id);
CREATE INDEX idx_sys_sys_form_a_def ON sys_system_form_value (attribute_id);
CREATE INDEX idx_sys_sys_form_stxt ON sys_system_form_value (short_text_value);
CREATE INDEX idx_sys_sys_form_uuid ON sys_system_form_value (uuid_value);


CREATE TABLE sys_system_mapping (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	can_be_acc_created_script text,
	entity_type varchar(255) NOT NULL,
	name varchar(255) NOT NULL,
	operation_type varchar(255) NOT NULL,
	protection_enabled bit,
	protection_interval int,
	object_class_id binary(16) NOT NULL,
	tree_type_id binary(16),
	CONSTRAINT sys_system_mapping_pkey PRIMARY KEY (id),
	CONSTRAINT sys_system_mapping_protection_interval_check CHECK ((protection_interval >= 0))
);
CREATE INDEX idx_sys_s_mapping_e_type ON sys_system_mapping (entity_type);
CREATE INDEX idx_sys_s_mapping_o_c_id ON sys_system_mapping (object_class_id);
CREATE INDEX idx_sys_s_mapping_o_type ON sys_system_mapping (operation_type);
CREATE UNIQUE INDEX ux_sys_s_mapping_name ON sys_system_mapping (name,object_class_id);
