--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Identity role parameters

CREATE TABLE acc_identity_role_account (
	id binary(16) NOT NULL,
	created datetime2 NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id  binary(16) NULL,
	modified datetime2 NULL,
	modifier nvarchar(255) NULL,
	modifier_id  binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id  binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id  binary(16) NULL,
	realm_id  binary(16) NULL,
	transaction_id  binary(16) NULL,
	account_id binary(16) NOT NULL,
	identity_role_id binary(16) NOT NULL,
	ownership bit NOT NULL,
	CONSTRAINT acc_identity_role_account_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_acc_iderole_acc_acc ON acc_identity_role_account (account_id);
CREATE INDEX idx_acc_iderole_acc_contr ON acc_identity_role_account (identity_role_id);

CREATE TABLE acc_identity_role_account_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint NULL,
	created datetime2 NULL,
	created_m bit NULL,
	creator nvarchar(255) NULL,
	creator_m bit NULL,
	creator_id  binary(16) NULL,
	creator_id_m bit NULL,
	modifier nvarchar(255) NULL,
	modifier_m bit NULL,
	modifier_id  binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator nvarchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id  binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id  binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id  binary(16) NULL,
	realm_id_m bit NULL,
	transaction_id  binary(16) NULL,
	transaction_id_m bit NULL,
	account_id binary(16) NULL,
	account_m bit NULL,
	identity_role_id binary(16) NULL,
	identity_role_m bit NULL,
	ownership bit NULL,
	ownership_m bit NULL
	CONSTRAINT acc_identity_role_account_a_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_rl8ie92omig5ksgd2mu6y22ng FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
