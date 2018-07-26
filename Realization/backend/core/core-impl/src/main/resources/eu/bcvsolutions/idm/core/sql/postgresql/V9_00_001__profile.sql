--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add identity profile - image, preffered language

CREATE TABLE idm_profile (
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
	preferred_language varchar(45) NULL,
	identity_id bytea NOT NULL,
	image_id bytea NULL,
	CONSTRAINT idm_profile_pkey PRIMARY KEY (id),
	CONSTRAINT uk_profile_identity_id UNIQUE (identity_id)
);

CREATE TABLE idm_profile_a (
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
	preferred_language varchar(45) NULL,
	preferred_language_m bool NULL,
	identity_id bytea NULL,
	identity_m bool NULL,
	image_id bytea NULL,
	image_m bool NULL,
	CONSTRAINT idm_profile_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_l70tqy66dd7lcx5by8egpg11a FOREIGN KEY (rev) REFERENCES idm_audit(id)
)