--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Add form projections

CREATE TABLE idm_form_projection (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea NULL,
	modified timestamp NULL,
	modifier varchar(255) NULL,
	modifier_id bytea NULL,
	original_creator varchar(255) NULL,
	original_creator_id bytea NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id bytea NULL,
	realm_id bytea NULL,
	transaction_id bytea NULL,
	code varchar(255) NOT NULL,
	owner_type varchar(255) NOT NULL,
	route varchar(255) NULL,
	description varchar(2000) NULL,
	module_id varchar(255) NULL,
	disabled bool NOT NULL DEFAULT false,
	form_definitions text NULL,
	basic_fields varchar(2000) NULL,
	projection_properties bytea NULL,
	CONSTRAINT idm_form_projection_pkey PRIMARY KEY (id),
	CONSTRAINT idx_idm_form_proj_code UNIQUE (code)
);
CREATE INDEX idx_idm_form_proj_owner_type ON idm_form_projection USING btree (owner_type);

CREATE TABLE idm_form_projection_a (
	id bytea NOT NULL,
	rev int8 NOT NULL,
	revtype int2 NULL,
	created timestamp NULL,
	created_m bool NULL,
	creator varchar(255) NULL,
	creator_m bool NULL,
	creator_id bytea NULL,
	creator_id_m bool NULL,
	modifier varchar(255) NULL,
	modifier_m bool NULL,
	modifier_id bytea NULL,
	modifier_id_m bool NULL,
	original_creator varchar(255) NULL,
	original_creator_m bool NULL,
	original_creator_id bytea NULL,
	original_creator_id_m bool NULL,
	original_modifier varchar(255) NULL,
	original_modifier_m bool NULL,
	original_modifier_id bytea NULL,
	original_modifier_id_m bool NULL,
	realm_id bytea NULL,
	realm_id_m bool NULL,
	code varchar(255) NULL,
	code_m bool NULL,
	owner_type varchar(255) NULL,
	owner_type_m bool NULL,
	route varchar(255) NULL,
	route_m bool NULL,
	description varchar(2000) NULL,
	description_m bool NULL,
	module_id varchar(255) NULL,
	module_m bool NULL,
	disabled bool NULL,
	disabled_m bool NULL,
	form_definitions text NULL,
	form_definitions_m bool NULL,
	basic_fields varchar(2000) NULL,
	basic_fields_m bool NULL,
	projection_properties bytea NULL,
	properties_m bool NULL,
	CONSTRAINT idm_form_projection_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk32ixqy0cfuganb4635m06mex2 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

ALTER TABLE idm_identity ADD form_projection_id bytea NULL;
ALTER TABLE idm_identity_a ADD form_projection_id bytea NULL;
ALTER TABLE idm_identity_a ADD form_projection_m bool NULL;

CREATE INDEX idx_idm_identity_form_proj ON idm_identity USING btree (form_projection_id);


