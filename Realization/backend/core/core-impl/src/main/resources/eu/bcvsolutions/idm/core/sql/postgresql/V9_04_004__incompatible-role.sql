--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add incompatible role agenda

CREATE TABLE idm_role_incompatible (
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
	external_id varchar(255) NULL,
	sub_id bytea NOT NULL,
	superior_id bytea NOT NULL,
	CONSTRAINT idm_role_incompatible_pkey PRIMARY KEY (id),
	CONSTRAINT ux_idm_role_incompatible_susu UNIQUE (superior_id, sub_id)
);
CREATE INDEX idx_idm_role_incompatible_e_id ON idm_role_incompatible USING btree (external_id) ;
CREATE INDEX idx_idm_role_incompatible_sub ON idm_role_incompatible USING btree (sub_id) ;
CREATE INDEX idx_idm_role_incompatible_super ON idm_role_incompatible USING btree (superior_id) ;

CREATE TABLE idm_role_incompatible_a (
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
	external_id varchar(255) NULL,
	external_id_m bool NULL,
	sub_id bytea NULL,
	sub_m bool NULL,
	superior_id bytea NULL,
	superior_m bool NULL,
	CONSTRAINT idm_role_incompatible_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_irmbitp1pv0h8c4x6oht5brfx FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
