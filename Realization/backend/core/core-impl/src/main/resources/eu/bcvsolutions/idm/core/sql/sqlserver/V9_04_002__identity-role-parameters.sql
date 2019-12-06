--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Identity role parameters

CREATE TABLE idm_concept_role_form_value (
    id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	boolean_value bit,
	byte_value varbinary(255),
	confidential bit NOT NULL,
	date_value datetime2(6),
	double_value numeric(38,4),
	long_value numeric(19,0),
	persistent_type nvarchar(45) NOT NULL,
	seq smallint,
	short_text_value nvarchar(2000),
	string_value nvarchar(MAX),
	uuid_value binary(16),
	attribute_id binary(16) NOT NULL,
	owner_id binary(16) NOT NULL,
	CONSTRAINT idm_concept_role_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_concept_role_form_value_seq_check CHECK ((seq <= 99999))
);

CREATE INDEX idx_concept_rol_form_a ON idm_concept_role_form_value (owner_id);
CREATE INDEX idx_concept_rol_form_a_def ON idm_concept_role_form_value (attribute_id);
CREATE INDEX idx_concept_rol_form_stxt ON idm_concept_role_form_value (short_text_value);
CREATE INDEX idx_concept_rol_form_uuid ON idm_concept_role_form_value (uuid_value);

CREATE TABLE idm_concept_role_form_value_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
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
	boolean_value bit,
	boolean_value_m bit,
	byte_value varbinary(255),
	byte_value_m bit,
	confidential bit,
	confidential_m bit,
	date_value datetime2(6),
	date_value_m bit,
	double_value numeric(38,4),
	double_value_m bit,
	long_value numeric(19,0),
	long_value_m bit,
	persistent_type nvarchar(45),
	persistent_type_m bit,
	seq smallint,
	seq_m bit,
	short_text_value nvarchar(2000),
	short_text_value_m bit,
	string_value nvarchar(MAX),
	string_value_m bit,
	uuid_value binary(16),
	uuid_value_m bit,
	attribute_id binary(16),
	form_attribute_m bit,
	owner_id binary(16),
	owner_m bit,
	CONSTRAINT idm_concept_role_form_value_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_t5r1wexotm521kj3ix9jprj9h FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

