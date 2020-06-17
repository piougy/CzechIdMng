--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Delegations

CREATE TABLE idm_delegation (
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
	owner_id bytea NOT NULL,
	owner_type varchar(255) NOT NULL,
	definition_id bytea NOT NULL,
	result_cause text NULL,
	result_code varchar(255) NULL,
	result_model bytea NULL,
	result_state varchar(45) NULL,
	CONSTRAINT idm_delegation_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_i_del_definition_id ON public.idm_delegation USING btree (definition_id);
CREATE INDEX idx_i_del_owner_id ON public.idm_delegation USING btree (owner_id);
CREATE INDEX idx_i_del_owner_type ON public.idm_delegation USING btree (owner_type);

CREATE TABLE idm_delegation_a (
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
	owner_id bytea NULL,
	owner_id_m bool NULL,
	owner_type varchar(255) NULL,
	owner_type_m bool NULL,
	definition_id bytea NULL,
	definition_m bool NULL,
	CONSTRAINT idm_delegation_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk8yeyrfs1hoy93tpwtr2twgxxk FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE idm_delegation_def (
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
	description varchar(2000) NULL,
	"type" varchar(255) NOT NULL,
	valid_from date NULL,
	valid_till date NULL,
	delegate_id bytea NOT NULL,
	delegator_contract_id bytea NULL,
	delegator_id bytea NOT NULL,
	CONSTRAINT idm_delegation_def_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_i_del_def_del_cont_id ON public.idm_delegation_def USING btree (delegator_contract_id);
CREATE INDEX idx_i_del_def_delegate_id ON public.idm_delegation_def USING btree (delegate_id);
CREATE INDEX idx_i_del_def_delegator_id ON public.idm_delegation_def USING btree (delegator_id);
CREATE INDEX idx_i_del_def_type ON public.idm_delegation_def USING btree (type);
CREATE INDEX idx_i_del_def_valid_from ON public.idm_delegation_def USING btree (valid_from);
CREATE INDEX idx_i_del_def_valid_till ON public.idm_delegation_def USING btree (valid_till);

CREATE TABLE idm_delegation_def_a (
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
	description varchar(2000) NULL,
	description_m bool NULL,
	"type" varchar(255) NULL,
	type_m bool NULL,
	valid_from date NULL,
	valid_from_m bool NULL,
	valid_till date NULL,
	valid_till_m bool NULL,
	delegate_id bytea NULL,
	delegate_m bool NULL,
	delegator_id bytea NULL,
	delegator_m bool NULL,
	delegator_contract_id bytea NULL,
	delegator_contract_m bool NULL,
	CONSTRAINT idm_delegation_def_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fkqncqwj40krnhvcwxid9gq5eif FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
