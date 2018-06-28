--
-- CzechIdM 7 Flyway script 
-- BCV solutions s.r.o.
--
-- Contract slices - realtion between account and slice

CREATE TABLE acc_contract_slice_account (
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
	contract_slice_id bytea NOT NULL,
	CONSTRAINT acc_contract_slice_account_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_acc_contr_sli_acc ON acc_contract_slice_account USING btree (account_id) ;
CREATE INDEX idx_acc_contr_sli_contr ON acc_contract_slice_account USING btree (contract_slice_id) ;

CREATE TABLE acc_contract_slice_account_a (
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
	contract_slice_id bytea NULL,
	slice_m bool NULL,
	CONSTRAINT acc_contract_slice_account_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_gfrmpwlq597v1c51v7huftfqj FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
