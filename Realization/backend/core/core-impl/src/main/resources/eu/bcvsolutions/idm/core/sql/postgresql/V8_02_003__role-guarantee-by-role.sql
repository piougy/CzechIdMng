--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Role guarantee by role
CREATE TABLE idm_role_guarantee_role (
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
	guarantee_role_id bytea NOT NULL,
	role_id bytea NOT NULL,
	CONSTRAINT idm_role_guarantee_role_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_role_g_r_g_role ON idm_role_guarantee_role USING btree (guarantee_role_id);
CREATE INDEX idx_idm_role_g_r_role ON idm_role_guarantee_role USING btree (role_id);

-- audit
CREATE TABLE public.idm_role_guarantee_role_a (
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
	guarantee_role_id bytea NULL,
	guarantee_role_m bool NULL,
	role_id bytea NULL,
	role_m bool NULL,
	CONSTRAINT idm_role_guarantee_role_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_52w0m9i93h9galqewk813qqd3 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);