--
-- CzechIdM 9.0 Flyway script
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM (module core)

CREATE TABLE idm_attachment (
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
	attachment_type varchar(50),
	content_id binary(255) NOT NULL,
	content_path varchar(512),
	description varchar(2000),
	encoding varchar(100) NOT NULL,
	filesize numeric(19,0) NOT NULL,
	mimetype varchar(255) NOT NULL,
	name varchar(255) NOT NULL,
	owner_id binary(16),
	owner_state varchar(50),
	owner_type varchar(255) NOT NULL,
	version_label varchar(10) NOT NULL,
	version_number int NOT NULL,
	next_version_id binary(16),
	parent_id binary(16),
	CONSTRAINT idm_attachment_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_attachment_desc ON idm_attachment (description);
CREATE INDEX idx_idm_attachment_name ON idm_attachment (name);
CREATE INDEX idx_idm_attachment_o_id ON idm_attachment (owner_id);
CREATE INDEX idx_idm_attachment_o_type ON idm_attachment (owner_type);


CREATE TABLE idm_authorization_policy (
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
	authorizable_type varchar(255),
	base_permissions varchar(255),
	description varchar(2000),
	disabled bit NOT NULL,
	evaluator_properties image,
	evaluator_type varchar(255) NOT NULL,
	group_permission varchar(255),
	seq smallint,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_authorization_policy_pkey PRIMARY KEY (id),
	CONSTRAINT idm_authorization_policy_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_author_policy_a_t ON idm_authorization_policy (authorizable_type);
CREATE INDEX idx_idm_author_policy_role ON idm_authorization_policy (role_id);


CREATE TABLE idm_auto_role (
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
	name varchar(255) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_auto_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_auto_role_name ON idm_auto_role (name);
CREATE INDEX idx_idm_auto_role_role ON idm_auto_role (role_id);


CREATE TABLE idm_auto_role_att_rule (
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
	attribute_name varchar(255),
	comparison varchar(255) NOT NULL,
	[type] varchar(255) NOT NULL,
	value varchar(2000),
	auto_role_att_id binary(16) NOT NULL,
	form_attribute_id binary(16),
	CONSTRAINT idm_auto_role_att_rule_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_auto_role_att_rule_id ON idm_auto_role_att_rule (auto_role_att_id);
CREATE INDEX idx_idm_auto_role_form_att_id ON idm_auto_role_att_rule (form_attribute_id);
CREATE INDEX idx_idm_auto_role_form_att_name ON idm_auto_role_att_rule (attribute_name);
CREATE INDEX idx_idm_auto_role_form_type ON idm_auto_role_att_rule ([type]);


CREATE TABLE idm_auto_role_att_rule_req (
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
	attribute_name varchar(255),
	comparison varchar(255),
	operation varchar(255) NOT NULL,
	[type] varchar(255),
	value varchar(2000),
	form_attribute_id binary(16),
	auto_role_att_id binary(16) NOT NULL,
	rule_id binary(16),
	CONSTRAINT idm_auto_role_att_rule_req_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_au_r_att_rule_id_req ON idm_auto_role_att_rule_req (auto_role_att_id);
CREATE INDEX idx_idm_au_r_att_rule_req_rule ON idm_auto_role_att_rule_req (rule_id);
CREATE INDEX idx_idm_au_r_form_att_id_req ON idm_auto_role_att_rule_req (form_attribute_id);
CREATE INDEX idx_idm_au_r_form_att_n_req ON idm_auto_role_att_rule_req (attribute_name);
CREATE INDEX idx_idm_au_r_form_type_req ON idm_auto_role_att_rule_req ([type]);


CREATE TABLE idm_auto_role_attribute (
	concept bit NOT NULL,
	id binary(16) NOT NULL,
	CONSTRAINT idm_auto_role_attribute_pkey PRIMARY KEY (id),
	CONSTRAINT fk_b8r7j4ssop819j82ebm29kdaq FOREIGN KEY (id) REFERENCES idm_auto_role(id)
);


CREATE TABLE idm_auto_role_request (
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
	description varchar(2000),
	execute_immediately bit NOT NULL,
	name varchar(255),
	operation varchar(255),
	recursion_type varchar(255),
	request_type varchar(255) NOT NULL,
	result_cause text,
	result_code varchar(255),
	result_model image,
	result_state varchar(45) NOT NULL,
	state varchar(255) NOT NULL,
	wf_process_id varchar(255),
	auto_role_att_id binary(16),
	role_id binary(16),
	tree_node_id binary(16),
	CONSTRAINT idm_auto_role_request_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_auto_role_name_req ON idm_auto_role_request (name);
CREATE INDEX idx_idm_auto_role_role_req ON idm_auto_role_request (role_id);


CREATE TABLE idm_con_slice_form_value (
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
	CONSTRAINT idm_con_slice_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_con_slice_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_con_slice_form_a ON idm_con_slice_form_value (owner_id);
CREATE INDEX idx_idm_con_slice_form_a_def ON idm_con_slice_form_value (attribute_id);
CREATE INDEX idx_idm_con_slice_form_stxt ON idm_con_slice_form_value (short_text_value);
CREATE INDEX idx_idm_con_slice_form_uuid ON idm_con_slice_form_value (uuid_value);


CREATE TABLE idm_concept_role_request (
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
	log text,
	operation varchar(255),
	state varchar(255) NOT NULL,
	valid_from datetime,
	valid_till datetime,
	wf_process_id varchar(255),
	automatic_role_id binary(16),
	identity_contract_id binary(16),
	identity_role_id binary(16),
	role_id binary(16),
	request_role_id binary(16) NOT NULL,
	CONSTRAINT idm_concept_role_request_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_conc_role_ident_c ON idm_concept_role_request (identity_contract_id);
CREATE INDEX idx_idm_conc_role_request ON idm_concept_role_request (request_role_id);
CREATE INDEX idx_idm_conc_role_role ON idm_concept_role_request (role_id);
CREATE INDEX idx_idm_conc_role_iden_rol ON idm_concept_role_request  (identity_role_id);
CREATE INDEX idx_idm_conc_role_tree_node ON idm_concept_role_request (automatic_role_id);


CREATE TABLE idm_confidential_storage (
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
	storage_key varchar(255) NOT NULL,
	owner_id binary(16) NOT NULL,
	owner_type varchar(255) NOT NULL,
	storage_value image,
	CONSTRAINT idm_confidential_storage_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_confidential_storage_key ON idm_confidential_storage (storage_key);
CREATE INDEX idx_confidential_storage_o_i ON idm_confidential_storage (owner_id);
CREATE INDEX idx_confidential_storage_o_t ON idm_confidential_storage (owner_type);


CREATE TABLE idm_configuration (
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
	confidential bit NOT NULL,
	name varchar(255) NOT NULL,
	secured bit NOT NULL,
	value varchar(255),
	CONSTRAINT idm_configuration_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_configuration_name ON idm_configuration (name);


CREATE TABLE idm_contract_guarantee (
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
	external_id varchar(255),
	guarantee_id binary(16) NOT NULL,
	identity_contract_id binary(16) NOT NULL,
	CONSTRAINT idm_contract_guarantee_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_contract_guarantee_contr ON idm_contract_guarantee (identity_contract_id);
CREATE INDEX idx_contract_guarantee_idnt ON idm_contract_guarantee (guarantee_id);
CREATE INDEX idx_idm_contract_guar_ext_id ON idm_contract_guarantee (external_id);


CREATE TABLE idm_contract_slice (
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
	contract_code varchar(255),
	contract_valid_from datetime,
	contract_valid_till datetime,
	description varchar(2000),
	disabled bit NOT NULL,
	external_id varchar(255),
	externe bit NOT NULL,
	main bit NOT NULL,
	[position] varchar(255),
	state varchar(45),
	using_as_contract bit NOT NULL,
	valid_from datetime,
	valid_till datetime,
	identity_id binary(16) NOT NULL,
	parent_contract_id binary(16),
	work_position_id binary(16),
	CONSTRAINT idm_contract_slice_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_contract_slice_ext_id ON idm_contract_slice (external_id);
CREATE INDEX idx_idm_contract_slice_idnt ON idm_contract_slice (identity_id);
CREATE INDEX idx_idm_contract_slice_wp ON idm_contract_slice (work_position_id);


CREATE TABLE idm_contract_slice_guarantee (
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
	contract_slice_id binary(16) NOT NULL,
	guarantee_id binary(16) NOT NULL,
	CONSTRAINT idm_contract_slice_guarantee_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_contract_slice_guar_contr ON idm_contract_slice_guarantee (contract_slice_id);
CREATE INDEX idx_contract_slice_guar_idnt ON idm_contract_slice_guarantee (guarantee_id);


CREATE TABLE idm_dependent_task_trigger (
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
	dependent_task_id varchar(255) NOT NULL,
	initiator_task_id varchar(255) NOT NULL,
	CONSTRAINT idm_dependent_task_trigger_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_dependent_t_dep ON idm_dependent_task_trigger (dependent_task_id);
CREATE INDEX idx_idm_dependent_t_init ON idm_dependent_task_trigger (initiator_task_id);


CREATE TABLE idm_entity_event (
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
	closed bit NOT NULL,
	content image,
	event_type varchar(255),
	execute_date datetime,
	instance_id varchar(255) NOT NULL,
	original_source image,
	owner_id binary(16) NOT NULL,
	owner_type varchar(255) NOT NULL,
	parent_event_type varchar(255),
	priority varchar(45) NOT NULL,
	processed_order int,
	properties image,
	result_cause text,
	result_code varchar(255),
	result_model image,
	result_state varchar(45) NOT NULL,
	suspended bit NOT NULL,
	parent_id binary(16),
	CONSTRAINT idm_entity_event_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_entity_event_created ON idm_entity_event (created);
CREATE INDEX idx_idm_entity_event_exe ON idm_entity_event (execute_date);
CREATE INDEX idx_idm_entity_event_inst ON idm_entity_event (instance_id);
CREATE INDEX idx_idm_entity_event_o_id ON idm_entity_event (owner_id);
CREATE INDEX idx_idm_entity_event_o_type ON idm_entity_event (owner_type);


CREATE TABLE idm_entity_state (
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
	closed bit NOT NULL,
	instance_id varchar(255) NOT NULL,
	owner_id binary(16) NOT NULL,
	owner_type varchar(255) NOT NULL,
	processed_order int,
	processor_id varchar(255),
	processor_module varchar(255),
	processor_name varchar(255),
	result_cause text,
	result_code varchar(255),
	result_model image,
	result_state varchar(45) NOT NULL,
	suspended bit NOT NULL,
	event_id binary(16),
	CONSTRAINT idm_entity_state_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_entity_state_event ON idm_entity_state (event_id);
CREATE INDEX idx_idm_entity_state_o_id ON idm_entity_state (owner_id);
CREATE INDEX idx_idm_entity_state_o_type ON idm_entity_state (owner_type);


CREATE TABLE idm_forest_index (
	id numeric(19,0) NOT NULL IDENTITY(1,1),
	forest_tree_type varchar(255) NOT NULL,
	lft numeric(19,0),
	rgt numeric(19,0),
	content_id binary(16),
	parent_id numeric(19,0),
	CONSTRAINT idm_forest_index_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_jrcdxstxj5wxfn3v0m5x2rcr4 ON idm_forest_index (content_id);
CREATE INDEX idx_forest_index_content ON idm_forest_index (content_id);
CREATE INDEX idx_forest_index_lft ON idm_forest_index (lft);
CREATE INDEX idx_forest_index_parent ON idm_forest_index (parent_id);
CREATE INDEX idx_forest_index_rgt ON idm_forest_index (rgt);
CREATE INDEX idx_forest_index_tree_type ON idm_forest_index (forest_tree_type);


CREATE TABLE idm_form (
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
	name varchar(255),
	owner_code varchar(255),
	owner_id binary(16),
	owner_type varchar(255) NOT NULL,
	form_definition_id binary(16) NOT NULL,
	CONSTRAINT idm_form_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_form_f_definition_id ON idm_form (form_definition_id);
CREATE INDEX idx_idm_form_owner_code ON idm_form (owner_code);
CREATE INDEX idx_idm_form_owner_id ON idm_form (owner_id);
CREATE INDEX idx_idm_form_owner_type ON idm_form (owner_type);


CREATE TABLE idm_form_attribute (
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
	code varchar(255) NOT NULL,
	confidential bit NOT NULL,
	default_value text,
	description varchar(2000),
	face_type varchar(45),
	multiple bit NOT NULL,
	name varchar(255) NOT NULL,
	persistent_type varchar(45) NOT NULL,
	placeholder varchar(255),
	readonly bit NOT NULL,
	required bit NOT NULL,
	seq smallint,
	unmodifiable bit NOT NULL,
	definition_id binary(16) NOT NULL,
	CONSTRAINT idm_form_attribute_pkey PRIMARY KEY (id),
	CONSTRAINT idm_form_attribute_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_f_a_definition_def ON idm_form_attribute (definition_id);
CREATE UNIQUE INDEX ux_idm_f_a_definition_name ON idm_form_attribute (definition_id,code);


CREATE TABLE idm_form_definition (
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
	code varchar(255) NOT NULL,
	description varchar(2000),
	main bit NOT NULL,
	name varchar(255) NOT NULL,
	definition_type varchar(255) NOT NULL,
	unmodifiable bit NOT NULL,
	CONSTRAINT idm_form_definition_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_idm_form_definition_tn ON idm_form_definition (definition_type,code);


CREATE TABLE idm_form_value (
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
	CONSTRAINT idm_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_form_value_a ON idm_form_value (owner_id);
CREATE INDEX idx_idm_form_value_a_def ON idm_form_value (attribute_id);
CREATE INDEX idx_idm_form_value_stxt ON idm_form_value (short_text_value);
CREATE INDEX idx_idm_form_value_uuid ON idm_form_value (uuid_value);


CREATE TABLE idm_i_contract_form_value (
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
	CONSTRAINT idm_i_contract_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_i_contract_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_i_contract_form_a ON idm_i_contract_form_value (owner_id);
CREATE INDEX idx_idm_i_contract_form_a_def ON idm_i_contract_form_value (attribute_id);
CREATE INDEX idx_idm_i_contract_form_stxt ON idm_i_contract_form_value (short_text_value);
CREATE INDEX idx_idm_i_contract_form_uuid ON idm_i_contract_form_value (uuid_value);


CREATE TABLE idm_identity (
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
	description varchar(2000),
	disabled bit NOT NULL,
	email varchar(255),
	external_code varchar(255),
	external_id varchar(255),
	first_name varchar(255),
	last_name varchar(255),
	phone varchar(30),
	state varchar(45) NOT NULL,
	title_after varchar(100),
	title_before varchar(100),
	username varchar(255) NOT NULL,
	version numeric(19,0),
	CONSTRAINT idm_identity_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_identity_external_code ON idm_identity (external_code);
CREATE INDEX idx_idm_identity_external_id ON idm_identity (external_id);
CREATE UNIQUE INDEX ux_idm_identity_username ON idm_identity (username);


CREATE TABLE idm_identity_contract (
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
	description varchar(2000),
	disabled bit NOT NULL,
	external_id varchar(255),
	externe bit NOT NULL,
	main bit NOT NULL,
	[position] varchar(255),
	state varchar(45),
	valid_from datetime,
	valid_till datetime,
	identity_id binary(16) NOT NULL,
	work_position_id binary(16),
	CONSTRAINT idm_identity_contract_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_contract_ext_id ON idm_identity_contract (external_id);
CREATE INDEX idx_idm_identity_contract_idnt ON idm_identity_contract (identity_id);
CREATE INDEX idx_idm_identity_contract_wp ON idm_identity_contract (work_position_id);


CREATE TABLE idm_identity_form_value (
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
	CONSTRAINT idm_identity_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_identity_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_identity_form_a ON idm_identity_form_value (owner_id);
CREATE INDEX idx_idm_identity_form_a_def ON idm_identity_form_value (attribute_id);
CREATE INDEX idx_idm_identity_form_stxt ON idm_identity_form_value (short_text_value);
CREATE INDEX idx_idm_identity_form_uuid ON idm_identity_form_value (uuid_value);


CREATE TABLE idm_identity_role (
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
	external_id varchar(255),
	valid_from datetime,
	valid_till datetime,
	automatic_role_id binary(16),
	identity_contract_id binary(16) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_identity_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_identity_role_aut_r ON idm_identity_role (automatic_role_id);
CREATE INDEX idx_idm_identity_role_ext_id ON idm_identity_role (external_id);
CREATE INDEX idx_idm_identity_role_ident_c ON idm_identity_role (identity_contract_id);
CREATE INDEX idx_idm_identity_role_role ON idm_identity_role (role_id);


CREATE TABLE idm_identity_role_valid_req (
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
	result_cause text,
	result_code varchar(255),
	result_model image,
	result_state varchar(45) NOT NULL,
	identity_role_id binary(16) NOT NULL,
	CONSTRAINT idm_identity_role_valid_request_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_cs0od1m7no03giio0p27mfpsr ON idm_identity_role_valid_req (identity_role_id);
CREATE INDEX idx_idm_identity_role_id ON idm_identity_role_valid_req (identity_role_id);


CREATE TABLE idm_long_running_task (
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
	task_count numeric(19,0),
	task_counter numeric(19,0),
	dry_run bit NOT NULL,
	instance_id varchar(255) NOT NULL,
	result_cause text,
	result_code varchar(255),
	result_model image,
	result_state varchar(45) NOT NULL,
	running bit NOT NULL,
	stateful bit NOT NULL,
	task_description varchar(255),
	task_properties image,
	task_started datetime,
	task_type varchar(255) NOT NULL,
	thread_id numeric(19,0) NOT NULL,
	thread_name varchar(255),
	scheduled_task_id binary(16),
	CONSTRAINT idm_long_running_task_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_long_r_t_inst ON idm_long_running_task (instance_id);
CREATE INDEX idx_idm_long_r_t_s_task ON idm_long_running_task (scheduled_task_id);
CREATE INDEX idx_idm_long_r_t_type ON idm_long_running_task (task_type);


CREATE TABLE idm_notification (
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
	html_message text,
	[level] varchar(45) NOT NULL,
	result_model image,
	subject varchar(255),
	text_message text,
	sent datetime,
	sent_log varchar(2000),
	topic varchar(255),
	identity_sender_id binary(16),
	notification_template_id binary(16),
	parent_notification_id binary(16),
	CONSTRAINT idm_notification_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_notification_parent ON idm_notification (parent_notification_id);
CREATE INDEX idx_idm_notification_sender ON idm_notification (identity_sender_id);


CREATE TABLE idm_notification_configuration (
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
	description varchar(2000),
	[level] varchar(45),
	notification_type varchar(255) NOT NULL,
	topic varchar(255) NOT NULL,
	template_id binary(16),
	CONSTRAINT idm_notification_configuration_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_not_conf_level ON idm_notification_configuration ([level]);
CREATE INDEX idx_idm_not_conf_topic ON idm_notification_configuration (topic);
CREATE INDEX idx_idm_not_conf_type ON idm_notification_configuration (notification_type);
CREATE INDEX idx_idm_not_template ON idm_notification_configuration (template_id);
CREATE UNIQUE INDEX ux_idm_not_conf ON idm_notification_configuration (topic,[level],notification_type);


CREATE TABLE idm_notification_log (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_log_pkey PRIMARY KEY (id),
	CONSTRAINT fk_6lxo8e33m2cn2kemxjfo72cp7 FOREIGN KEY (id) REFERENCES idm_notification(id)
);


CREATE TABLE idm_notification_console (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_console_pkey PRIMARY KEY (id),
	CONSTRAINT fk_ptf0bbum1akrs8sx6eoy0csw1 FOREIGN KEY (id) REFERENCES idm_notification_log(id)
);


CREATE TABLE idm_notification_email (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_email_pkey PRIMARY KEY (id),
	CONSTRAINT fk_675yat9emstk1gse1nybduyly FOREIGN KEY (id) REFERENCES idm_notification_log(id)
);


CREATE TABLE idm_notification_recipient (
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
	real_recipient varchar(255),
	identity_recipient_id binary(16),
	notification_id binary(16) NOT NULL,
	CONSTRAINT idm_notification_recipient_pkey PRIMARY KEY (id),
	CONSTRAINT fk_svipm8scpjnuy2keri4u1whf1 FOREIGN KEY (notification_id) REFERENCES idm_notification(id)
);
CREATE INDEX idx_idm_notification_rec_idnt ON idm_notification_recipient (identity_recipient_id);
CREATE INDEX idx_idm_notification_rec_not ON idm_notification_recipient (notification_id);


CREATE TABLE idm_notification_sms (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_sms_pkey PRIMARY KEY (id),
	CONSTRAINT fk_1t9lptl1wo4fdo5vux54lru6 FOREIGN KEY (id) REFERENCES idm_notification_log(id)
);


CREATE TABLE idm_notification_template (
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
	body_html text,
	body_text text,
	code varchar(255) NOT NULL,
	module varchar(255),
	name varchar(255) NOT NULL,
	[parameter] varchar(255),
	sender varchar(255),
	subject varchar(255) NOT NULL,
	unmodifiable bit NOT NULL,
	CONSTRAINT idm_notification_template_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_n_template_name ON idm_notification_template (name);
CREATE UNIQUE INDEX ux_idm_notification_template_code ON idm_notification_template (code);


CREATE TABLE idm_notification_websocket (
	id binary(16) NOT NULL,
	CONSTRAINT idm_notification_websocket_pkey PRIMARY KEY (id),
	CONSTRAINT fk_bnnrv7yx0p8mj75tn6ogkc4qe FOREIGN KEY (id) REFERENCES idm_notification_log(id)
);


CREATE TABLE idm_password (
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
	block_login_date datetime,
	last_successful_login datetime,
	must_change bit,
	password varchar(255),
	unsuccessful_attempts int NOT NULL,
	valid_from datetime,
	valid_till datetime,
	identity_id binary(16) NOT NULL,
	CONSTRAINT idm_password_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_idm_password_identity ON idm_password (identity_id);


CREATE TABLE idm_password_history (
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
	password varchar(255) NOT NULL,
	identity_id binary(16) NOT NULL,
	CONSTRAINT idm_password_history_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_identity ON idm_password_history (identity_id);


CREATE TABLE idm_password_policy (
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
	block_login_time int,
	default_policy bit NOT NULL,
	description varchar(2000),
	disabled bit NOT NULL,
	enchanced_control bit NOT NULL,
	generate_type varchar(255),
	identity_attribute_check varchar(255),
	lower_char_base varchar(255) NOT NULL,
	lower_char_required bit NOT NULL,
	max_history_similar int,
	max_password_age int,
	max_password_length int,
	max_unsuccessful_attempts int,
	min_lower_char int,
	min_number int,
	min_password_age int,
	min_password_length int,
	min_rules_to_fulfill int,
	min_special_char int,
	min_upper_char int,
	name varchar(255) NOT NULL,
	number_base varchar(255) NOT NULL,
	number_required bit NOT NULL,
	passphrase_words int,
	password_length_required bit NOT NULL,
	prohibited_characters varchar(255),
	special_char_base varchar(255) NOT NULL,
	special_char_required bit NOT NULL,
	[type] varchar(255),
	upper_char_base varchar(255) NOT NULL,
	upper_char_required bit NOT NULL,
	weak_pass varchar(255),
	weak_pass_required bit NOT NULL,
	CONSTRAINT idm_password_policy_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_idm_pass_policy_name ON idm_password_policy (name);


CREATE TABLE idm_processed_task_item (
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
	result_cause text,
	result_code varchar(255),
	result_model image,
	result_state varchar(45) NOT NULL,
	referenced_dto_type varchar(255) NOT NULL,
	referenced_entity_id binary(16) NOT NULL,
	long_running_task binary(16),
	scheduled_task_queue_owner binary(16),
	CONSTRAINT idm_processed_task_item_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_processed_t_i_l_r_t ON idm_processed_task_item (long_running_task);
CREATE INDEX idm_processed_t_i_q_o ON idm_processed_task_item (scheduled_task_queue_owner);


CREATE TABLE idm_profile (
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
	preferred_language varchar(45),
	identity_id binary(16) NOT NULL,
	image_id binary(16),
	CONSTRAINT idm_profile_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_profile_identity_id ON idm_profile (identity_id);


CREATE TABLE idm_role (
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
	approve_remove bit NOT NULL,
	can_be_requested bit NOT NULL,
	description varchar(2000),
	disabled bit NOT NULL,
	external_id varchar(255),
	name varchar(255) NOT NULL,
	priority int NOT NULL,
	role_type varchar(255) NOT NULL,
	version numeric(19,0),
	CONSTRAINT idm_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_external_id ON idm_role (external_id);
CREATE UNIQUE INDEX ux_idm_role_name ON idm_role (name);


CREATE TABLE idm_role_catalogue (
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
	code varchar(255) NOT NULL,
	description varchar(2000),
	external_id varchar(255),
	name varchar(255) NOT NULL,
	url varchar(255),
	url_title varchar(255),
	parent_id binary(16),
	CONSTRAINT idm_role_catalogue_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_cat_ext_id ON idm_role_catalogue (external_id);
CREATE INDEX idx_idm_role_cat_parent ON idm_role_catalogue (parent_id);
CREATE UNIQUE INDEX ux_role_catalogue_code ON idm_role_catalogue (code);
CREATE INDEX ux_role_catalogue_name ON idm_role_catalogue (name);


CREATE TABLE idm_role_catalogue_role (
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
	role_id binary(16) NOT NULL,
	role_catalogue_id binary(16) NOT NULL,
	CONSTRAINT idm_role_catalogue_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_catalogue_id ON idm_role_catalogue_role (role_catalogue_id);
CREATE INDEX idx_idm_role_id ON idm_role_catalogue_role (role_id);


CREATE TABLE idm_role_composition (
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
	sub_id binary(16) NOT NULL,
	superior_id binary(16) NOT NULL,
	CONSTRAINT idm_role_composition_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_composition_sub ON idm_role_composition (sub_id);
CREATE INDEX idx_idm_role_composition_super ON idm_role_composition (superior_id);


CREATE TABLE idm_role_form_value (
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
	CONSTRAINT idm_role_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_role_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_role_form_a ON idm_role_form_value (owner_id);
CREATE INDEX idx_idm_role_form_a_def ON idm_role_form_value (attribute_id);
CREATE INDEX idx_idm_role_form_stxt ON idm_role_form_value (short_text_value);
CREATE INDEX idx_idm_role_form_uuid ON idm_role_form_value (uuid_value);


CREATE TABLE idm_role_guarantee (
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
	guarantee_id binary(16) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_role_guarantee_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_guarantee_gnt ON idm_role_guarantee (guarantee_id);
CREATE INDEX idx_idm_role_guarantee_role ON idm_role_guarantee (role_id);


CREATE TABLE idm_role_guarantee_role (
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
	guarantee_role_id binary(16) NOT NULL,
	role_id binary(16) NOT NULL,
	CONSTRAINT idm_role_guarantee_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_g_r_g_role ON idm_role_guarantee_role (guarantee_role_id);
CREATE INDEX idx_idm_role_g_r_role ON idm_role_guarantee_role (role_id);


CREATE TABLE idm_role_request (
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
	description varchar(255),
	execute_immediately bit NOT NULL,
	log text,
	original_request text,
	requested_by_type varchar(255) NOT NULL,
	state varchar(255) NOT NULL,
	wf_process_id varchar(255),
	applicant_id binary(16) NOT NULL,
	duplicated_to_request binary(16),
	CONSTRAINT idm_role_request_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_request_app_id ON idm_role_request (applicant_id);
CREATE INDEX idx_idm_role_request_state ON idm_role_request (state);


CREATE TABLE idm_role_tree_node (
	recursion_type varchar(255) NOT NULL,
	id binary(16) NOT NULL,
	tree_node_id binary(16) NOT NULL,
	CONSTRAINT idm_role_tree_node_pkey PRIMARY KEY (id),
	CONSTRAINT fk_2qgkyu38e9u67xtedpr0ylqb5 FOREIGN KEY (id) REFERENCES idm_auto_role(id)
);
CREATE INDEX idx_idm_role_tree_node ON idm_role_tree_node (tree_node_id);


CREATE TABLE idm_scheduled_task (
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
	quartz_task_name varchar(255) NOT NULL,
	CONSTRAINT idm_scheduled_task_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_8bbpr92i3lvuiw52kvmh8ci1c ON idm_scheduled_task (quartz_task_name);


CREATE TABLE idm_script (
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
	category varchar(255) NOT NULL,
	code varchar(255) NOT NULL,
	description varchar(2000),
	name varchar(255),
	script text,
	CONSTRAINT idm_script_pkey PRIMARY KEY (id)
);
CREATE INDEX ux_script_category ON idm_script (category);
CREATE UNIQUE INDEX ux_script_code ON idm_script (code);


CREATE TABLE idm_script_authority (
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
	class_name varchar(255),
	service varchar(255),
	[type] varchar(255) NOT NULL,
	script_id binary(16) NOT NULL,
	CONSTRAINT idm_script_authority_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_script_auth_script ON idm_script_authority (script_id);


CREATE TABLE idm_token (
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
	disabled bit NOT NULL,
	expiration datetime,
	external_id varchar(255),
	issued_at datetime NOT NULL,
	module_id varchar(255),
	owner_id binary(16),
	owner_type varchar(255) NOT NULL,
	properties image,
	token varchar(2000),
	token_type varchar(45),
	CONSTRAINT idm_token_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_token_exp ON idm_token (expiration);
CREATE INDEX idx_idm_token_external_id ON idm_token (external_id);
CREATE INDEX idx_idm_token_o_id ON idm_token (owner_id);
CREATE INDEX idx_idm_token_o_type ON idm_token (owner_type);
CREATE INDEX idx_idm_token_token ON idm_token (token);
CREATE INDEX idx_idm_token_type ON idm_token (token_type);


CREATE TABLE idm_tree_node (
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
	code varchar(255) NOT NULL,
	disabled bit NOT NULL,
	external_id varchar(255),
	name varchar(255) NOT NULL,
	version numeric(19,0),
	parent_id binary(16),
	tree_type_id binary(16) NOT NULL,
	CONSTRAINT idm_tree_node_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_tree_node_ext_id ON idm_tree_node (external_id);
CREATE INDEX idx_idm_tree_node_parent ON idm_tree_node (parent_id);
CREATE INDEX idx_idm_tree_node_type ON idm_tree_node (tree_type_id);
CREATE UNIQUE INDEX ux_tree_node_code ON idm_tree_node (tree_type_id,code);


CREATE TABLE idm_tree_node_form_value (
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
	CONSTRAINT idm_tree_node_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_tree_node_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_tree_node_form_a ON idm_tree_node_form_value (owner_id);
CREATE INDEX idx_idm_tree_node_form_a_def ON idm_tree_node_form_value (attribute_id);
CREATE INDEX idx_idm_tree_node_form_stxt ON idm_tree_node_form_value (short_text_value);
CREATE INDEX idx_idm_tree_node_form_uuid ON idm_tree_node_form_value (uuid_value);


CREATE TABLE idm_tree_type (
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
	code varchar(255) NOT NULL,
	external_id varchar(255),
	name varchar(255) NOT NULL,
	CONSTRAINT idm_tree_type_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_tree_type_ext_id ON idm_tree_type (external_id);
CREATE UNIQUE INDEX ux_tree_type_code ON idm_tree_type (code);
CREATE INDEX ux_tree_type_name ON idm_tree_type (name);
