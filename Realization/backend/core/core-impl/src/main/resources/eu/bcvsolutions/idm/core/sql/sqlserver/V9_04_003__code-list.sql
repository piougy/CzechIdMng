--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add code lists agenda

CREATE TABLE idm_code_list (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(6) NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	code nvarchar(255) NOT NULL,
	name nvarchar(255) NOT NULL,
	form_definition_id binary(16) NOT NULL,
	description nvarchar(2000) NULL,
	external_id nvarchar(255) NULL,
	CONSTRAINT idm_code_list_pkey PRIMARY KEY (id),
	CONSTRAINT ux_idm_code_list_code UNIQUE (code)
);
CREATE INDEX idx_idm_code_list_ext_id ON idm_code_list (external_id) ;
CREATE INDEX idx_idm_code_list_f_def_id ON idm_code_list (form_definition_id) ;

CREATE TABLE idm_code_list_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint NULL,
	created datetime2(6) NULL,
	created_m bit NULL,
	creator nvarchar(255) NULL,
	creator_m bit NULL,
	creator_id binary(16) NULL,
	creator_id_m bit NULL,
	modifier nvarchar(255) NULL,
	modifier_m bit NULL,
	modifier_id binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator nvarchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id binary(16) NULL,
	realm_id_m bit NULL,
	transaction_id binary(16) NULL,
	transaction_id_m bit NULL,
	code nvarchar(255) NULL,
	code_m bit NULL,
	name nvarchar(255) NULL,
	name_m bit NULL,
	description nvarchar(2000) NULL,
	description_m bit NULL,
	external_id nvarchar(255) NULL,
	external_id_m bit NULL,
	CONSTRAINT idm_code_list_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_mjxqvq89ih5fakk9ri8v56v6r FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE idm_code_list_item (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(6) NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	code nvarchar(255) NOT NULL,
	name nvarchar(255) NOT NULL,
	code_list_id binary(16) NOT NULL,
	description nvarchar(2000) NULL,
	external_id nvarchar(255) NULL,
	"level" nvarchar(45) NULL,
	icon nvarchar(255) NULL,
	CONSTRAINT idm_code_list_item_pkey PRIMARY KEY (id),
	CONSTRAINT ux_idm_code_l_i_list_code UNIQUE (code_list_id, code)
);
CREATE INDEX idx_idm_code_l_i_code ON idm_code_list_item (code) ;
CREATE INDEX idx_idm_code_l_i_codelist ON idm_code_list_item (code_list_id) ;
CREATE INDEX idx_idm_code_l_i_ext_id ON idm_code_list_item (external_id) ;

CREATE TABLE idm_code_list_item_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint NULL,
	created datetime2(6) NULL,
	created_m bit NULL,
	creator nvarchar(255) NULL,
	creator_m bit NULL,
	creator_id binary(16) NULL,
	creator_id_m bit NULL,
	modifier nvarchar(255) NULL,
	modifier_m bit NULL,
	modifier_id binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator nvarchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id binary(16) NULL,
	realm_id_m bit NULL,
	transaction_id binary(16) NULL,
	transaction_id_m bit NULL,
	code nvarchar(255) NULL,
	code_m bit NULL,
	name nvarchar(255) NULL,
	name_m bit NULL,
	description nvarchar(2000) NULL,
	description_m bit NULL,
	external_id nvarchar(255) NULL,
	external_id_m bit NULL,
	"level" nvarchar(45) NULL,
	level_m bit NULL,
	icon nvarchar(255) NULL,
	icon_m bit NULL,
	CONSTRAINT idm_code_list_item_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_8gw0vr5qekosc9edf0defhtig FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE idm_code_list_item_value (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(6) NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	boolean_value bit NULL,
	byte_value varbinary(255) NULL,
	confidential bit NOT NULL,
	date_value datetime2(6) NULL,
	double_value numeric(38,4) NULL,
	long_value numeric(19,0) NULL,
	persistent_type nvarchar(45) NOT NULL,
	seq smallint NULL,
	short_text_value nvarchar(2000) NULL,
	string_value nvarchar(MAX) NULL,
	uuid_value binary(16) NULL,
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT idm_code_list_item_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_code_list_item_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_code_l_i_value_a ON idm_code_list_item_value (owner_id) ;
CREATE INDEX idx_idm_code_l_i_value_a_def ON idm_code_list_item_value (attribute_id) ;
CREATE INDEX idx_idm_code_l_i_value_stxt ON idm_code_list_item_value (short_text_value) ;
CREATE INDEX idx_idm_code_l_i_value_uuid ON idm_code_list_item_value (uuid_value) ;

CREATE TABLE idm_code_list_item_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint NULL,
	created datetime2(6) NULL,
	created_m bit NULL,
	creator nvarchar(255) NULL,
	creator_m bit NULL,
	creator_id binary(16) NULL,
	creator_id_m bit NULL,
	modifier nvarchar(255) NULL,
	modifier_m bit NULL,
	modifier_id binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator nvarchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id binary(16) NULL,
	realm_id_m bit NULL,
	transaction_id binary(16) NULL,
	transaction_id_m bit NULL,
	boolean_value bit NULL,
	boolean_value_m bit NULL,
	byte_value varbinary(255) NULL,
	byte_value_m bit NULL,
	confidential bit NULL,
	confidential_m bit NULL,
	date_value datetime2(6) NULL,
	date_value_m bit NULL,
	double_value numeric(38,4) NULL,
	double_value_m bit NULL,
	long_value numeric(19,0) NULL,
	long_value_m bit NULL,
	persistent_type nvarchar(45) NULL,
	persistent_type_m bit NULL,
	seq smallint NULL,
	seq_m bit NULL,
	short_text_value nvarchar(2000) NULL,
	short_text_value_m bit NULL,
	string_value nvarchar(MAX) NULL,
	string_value_m bit NULL,
	uuid_value binary(16) NULL,
	uuid_value_m bit NULL,
	attribute_id binary(16) NULL,
	form_attribute_m bit NULL,
	owner_id binary(16) NULL,
	owner_m bit NULL,
	CONSTRAINT idm_code_list_item_value_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_q5g131cvhcewy2or0tp4acar5 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
