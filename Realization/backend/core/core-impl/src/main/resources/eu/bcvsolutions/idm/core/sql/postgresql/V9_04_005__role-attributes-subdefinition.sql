--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add role attributes sub-definition agenda

CREATE TABLE idm_role_form_attribute (
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
	default_value text NULL,
	attribute_id bytea NOT NULL,
	role_id bytea NOT NULL,
	validation_max numeric(38,4) NULL,
	validation_min numeric(38,4) NULL,
	validation_regex varchar(2000) NULL,
	required bool NOT NULL,
	validation_unique bool NOT NULL,
	CONSTRAINT idm_role_form_attribute_pkey PRIMARY KEY (id),
	CONSTRAINT ux_idm_role_form_att_r_a UNIQUE (attribute_id, role_id)
);
CREATE INDEX idx_idm_role_form_att_def ON idm_role_form_attribute USING btree (attribute_id);
CREATE INDEX idx_idm_role_form_role ON idm_role_form_attribute USING btree (role_id);

CREATE TABLE idm_role_form_attribute_a (
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
	transaction_id bytea NULL,
	transaction_id_m bool NULL,
	default_value text NULL,
	default_value_m bool NULL,
	attribute_id bytea NULL,
	form_attribute_m bool NULL,
	role_id bytea NULL,
	role_m bool NULL,
	validation_max numeric(38,4) NULL,
	max_m bool NULL,
	validation_min numeric(38,4) NULL,
	min_m bool NULL,
	validation_regex varchar(2000) NULL,
	regex_m bool NULL,
	required bool NULL,
	required_m bool NULL,
	validation_unique bool NULL,
	unique_m bool NULL,
	CONSTRAINT idm_role_form_attribute_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_lpie4qss4rv6q7pjyu7m3sgll FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


