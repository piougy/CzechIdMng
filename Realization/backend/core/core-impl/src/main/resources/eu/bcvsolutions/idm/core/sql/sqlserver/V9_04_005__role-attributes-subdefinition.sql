--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add role attributes sub-definition agenda

CREATE TABLE idm_role_form_attribute (
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
	default_value nvarchar(MAX) NULL,
	attribute_id binary(16) NOT NULL,
	role_id binary(16) NOT NULL,
	validation_max numeric(38,4) NULL,
	validation_min numeric(38,4) NULL,
	validation_regex nvarchar(2000) NULL,
	required bit NOT NULL,
	validation_unique bit NOT NULL,
	CONSTRAINT idm_role_form_attribute_pkey PRIMARY KEY (id),
	CONSTRAINT ux_idm_role_form_att_r_a UNIQUE (attribute_id, role_id)
);
CREATE INDEX idx_idm_role_form_att_def ON idm_role_form_attribute (attribute_id);
CREATE INDEX idx_idm_role_form_role ON idm_role_form_attribute (role_id);

CREATE TABLE idm_role_form_attribute_a (
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
	default_value nvarchar(MAX) NULL,
	default_value_m bit NULL,
	attribute_id binary(16) NULL,
	form_attribute_m bit NULL,
	role_id binary(16) NULL,
	role_m bit NULL,
	validation_max numeric(38,4) NULL,
	max_m bit NULL,
	validation_min numeric(38,4) NULL,
	min_m bit NULL,
	validation_regex nvarchar(2000) NULL,
	regex_m bit NULL,
	required bit NULL,
	required_m bit NULL,
	validation_unique bit NULL,
	unique_m bit NULL,
	CONSTRAINT idm_role_form_attribute_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_lpie4qss4rv6q7pjyu7m3sgll FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


