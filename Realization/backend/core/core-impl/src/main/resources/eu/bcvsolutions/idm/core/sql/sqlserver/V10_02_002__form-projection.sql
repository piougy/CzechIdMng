--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add form projections

CREATE TABLE idm_form_projection (
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
	owner_type nvarchar(255) NOT NULL,
	route nvarchar(255) NULL,
	description nvarchar(2000) NULL,
	module_id nvarchar(255) NULL,
	disabled bit NOT NULL DEFAULT 0,
	form_definitions nvarchar(MAX) NULL,
	basic_fields nvarchar(2000) NULL,
	projection_properties binary(16) NULL,
	CONSTRAINT idm_form_projection_pkey PRIMARY KEY (id),
	CONSTRAINT idx_idm_form_proj_code UNIQUE (code)
);
CREATE INDEX idx_idm_form_proj_owner_type ON idm_form_projection(owner_type);

CREATE TABLE idm_form_projection_a (
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
	code nvarchar(255) NULL,
	code_m bit NULL,
	owner_type nvarchar(255) NULL,
	owner_type_m bit NULL,
	route nvarchar(255) NULL,
	route_m bit NULL,
	description nvarchar(2000) NULL,
	description_m bit NULL,
	module_id nvarchar(255) NULL,
	module_m bit NULL,
	disabled bit NULL,
	disabled_m bit NULL,
	form_definitions nvarchar(MAX) NULL,
	form_definitions_m bit NULL,
	basic_fields nvarchar(2000) NULL,
	basic_fields_m bit NULL,
	projection_properties binary(16) NULL,
	properties_m bit NULL,
	CONSTRAINT idm_form_projection_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk32ixqy0cfuganb4635m06mex2 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

ALTER TABLE idm_identity ADD form_projection_id binary(16) NULL;
ALTER TABLE idm_identity_a ADD form_projection_id binary(16) NULL;
ALTER TABLE idm_identity_a ADD form_projection_m bit NULL;

CREATE INDEX idx_idm_identity_form_proj ON idm_identity(form_projection_id);


