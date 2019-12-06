--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add incompatible role agenda

CREATE TABLE idm_role_incompatible (
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
	external_id nvarchar(255) NULL,
	sub_id binary(16) NOT NULL,
	superior_id binary(16) NOT NULL,
	CONSTRAINT idm_role_incompatible_pkey PRIMARY KEY (id),
	CONSTRAINT ux_idm_role_incompatible_susu UNIQUE (superior_id, sub_id)
);
CREATE INDEX idx_idm_role_incompatible_e_id ON idm_role_incompatible (external_id) ;
CREATE INDEX idx_idm_role_incompatible_sub ON idm_role_incompatible (sub_id) ;
CREATE INDEX idx_idm_role_incompatible_super ON idm_role_incompatible (superior_id) ;

CREATE TABLE idm_role_incompatible_a (
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
	external_id nvarchar(255) NULL,
	external_id_m bit NULL,
	sub_id binary(16) NULL,
	sub_m bit NULL,
	superior_id binary(16) NULL,
	superior_m bit NULL,
	CONSTRAINT idm_role_incompatible_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_irmbitp1pv0h8c4x6oht5brfx FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
