--
-- CzechIdM 9.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM (module vs)




CREATE TABLE vs_account (
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
	connector_key varchar(255) NOT NULL,
	enable bit NOT NULL,
	system_id binary(255) NOT NULL,
	uid varchar(255) NOT NULL,
	CONSTRAINT vs_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_vs_account_system ON vs_account (system_id);
CREATE INDEX idx_vs_account_uid ON vs_account (uid);
CREATE UNIQUE INDEX ux_vs_account_uid ON vs_account (uid,system_id,connector_key);


CREATE TABLE vs_account_form_value (
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
	CONSTRAINT vs_account_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT vs_account_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_vs_account_form_a ON vs_account_form_value (owner_id);
CREATE INDEX idx_vs_account_form_a_def ON vs_account_form_value (attribute_id);
CREATE INDEX idx_vs_account_form_stxt ON vs_account_form_value (short_text_value);
CREATE INDEX idx_vs_account_form_uuid ON vs_account_form_value (uuid_value);


CREATE TABLE vs_request (
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
	connector_conf image,
	connector_key varchar(255) NOT NULL,
	connector_object image,
	duplicate_to_request_id binary(255),
	execute_immediately bit NOT NULL,
	operation_type varchar(255) NOT NULL,
	reason varchar(255),
	state varchar(255) NOT NULL,
	uid varchar(255) NOT NULL,
	previous_request_id binary(16),
	system_id binary(16) NOT NULL,
	CONSTRAINT vs_request_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_vs_request_system ON vs_request (system_id);
CREATE INDEX idx_vs_request_uid ON vs_request (uid);


CREATE TABLE vs_system_implementer (
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
	identity_id binary(16),
	role_id binary(16),
	system_id binary(16) NOT NULL,
	CONSTRAINT vs_system_implementer_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_vs_sys_imple_identity ON vs_system_implementer (identity_id);
CREATE INDEX idx_vs_sys_imple_role ON vs_system_implementer (role_id);
CREATE INDEX idx_vs_sys_imple_system ON vs_system_implementer (system_id);
