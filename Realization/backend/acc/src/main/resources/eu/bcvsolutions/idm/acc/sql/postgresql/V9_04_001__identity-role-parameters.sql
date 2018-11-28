--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add attributes for role

CREATE TABLE acc_identity_role_account (
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
    ownership bool NOT NULL,
    account_id bytea NOT NULL,
    identity_role_id bytea NOT NULL,
    CONSTRAINT acc_identity_role_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_iderole_acc_acc ON acc_identity_role_account USING btree (account_id);
CREATE INDEX idx_acc_iderole_acc_contr ON acc_identity_role_account USING btree (identity_role_id);

CREATE TABLE acc_identity_role_account_a (
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
    ownership bool NULL,
    ownership_m bool NULL,
    account_id bytea NULL,
    account_m bool NULL,
    identity_role_id bytea NULL,
    identity_role_m bool NULL,
    CONSTRAINT acc_identity_role_account_a_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fk_rl8ie92omig5ksgd2mu6y22ng FOREIGN KEY (rev) REFERENCES idm_audit(id)
);