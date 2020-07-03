--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Delegations

CREATE TABLE idm_delegation (
	id binary(16) NOT NULL,
	created datetime2(7) NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(7) NULL,
	modifier varchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator varchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	owner_id binary(255) NOT NULL,
	result_cause varchar(MAX) NULL,
	result_code varchar(255) NULL,
	result_model varbinary NULL,
	result_state varchar(45) NULL,
	owner_type varchar(255) NOT NULL,
	definition_id binary(16) NOT NULL,
	CONSTRAINT PK__idm_dele__3213E83FFEDA4E20 PRIMARY KEY (id)
);
CREATE INDEX idx_i_del_definition_id ON idm_delegation (definition_id);
CREATE INDEX idx_i_del_owner_id ON idm_delegation (owner_id);
CREATE INDEX idx_i_del_owner_type ON idm_delegation (owner_type);

CREATE TABLE idm_delegation_a (
	id binary(16) NOT NULL,
	rev bigint NOT NULL,
	revtype smallint NULL,
	created datetime2(7) NULL,
	created_m bit NULL,
	creator varchar(255) NULL,
	creator_m bit NULL,
	creator_id binary(16) NULL,
	creator_id_m bit NULL,
	modifier varchar(255) NULL,
	modifier_m bit NULL,
	modifier_id binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator varchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier varchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id binary(16) NULL,
	realm_id_m bit NULL,
	owner_id binary(255) NULL,
	owner_id_m bit NULL,
	owner_type varchar(255) NULL,
	owner_type_m bit NULL,
	definition_id binary(16) NULL,
	definition_m bit NULL,
	CONSTRAINT PK__idm_dele__BE3894F9FF750A63 PRIMARY KEY (id,rev)
);

CREATE TABLE idm_delegation_def (
	id binary(16) NOT NULL,
	created datetime2(7) NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(7) NULL,
	modifier varchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator varchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	description varchar(2000) NULL,
	[type] varchar(255) NOT NULL,
	valid_from date NULL,
	valid_till date NULL,
	delegate_id binary(16) NOT NULL,
	delegator_id binary(16) NOT NULL,
	delegator_contract_id binary(16) NULL,
	CONSTRAINT PK__idm_dele__3213E83F7E821BE2 PRIMARY KEY (id)
);
CREATE INDEX idx_i_del_def_del_cont_id ON idm_delegation_def (delegator_contract_id);
CREATE INDEX idx_i_del_def_delegate_id ON idm_delegation_def (delegate_id);
CREATE INDEX idx_i_del_def_delegator_id ON idm_delegation_def (delegator_id);
CREATE INDEX idx_i_del_def_type ON idm_delegation_def ([type]);
CREATE INDEX idx_i_del_def_valid_from ON idm_delegation_def (valid_from);
CREATE INDEX idx_i_del_def_valid_till ON idm_delegation_def (valid_till);

CREATE TABLE idm_delegation_def_a (
	id binary(16) NOT NULL,
	rev bigint NOT NULL,
	revtype smallint NULL,
	created datetime2(7) NULL,
	created_m bit NULL,
	creator varchar(255) NULL,
	creator_m bit NULL,
	creator_id binary(16) NULL,
	creator_id_m bit NULL,
	modifier varchar(255) NULL,
	modifier_m bit NULL,
	modifier_id binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator varchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier varchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id binary(16) NULL,
	realm_id_m bit NULL,
	description varchar(2000) NULL,
	description_m bit NULL,
	[type] varchar(255) NULL,
	type_m bit NULL,
	valid_from date NULL,
	valid_from_m bit NULL,
	valid_till date NULL,
	valid_till_m bit NULL,
	delegate_id binary(16) NULL,
	delegate_m bit NULL,
	delegator_id binary(16) NULL,
	delegator_m bit NULL,
	delegator_contract_id binary(16) NULL,
	delegator_contract_m bit NULL,
	CONSTRAINT PK__idm_dele__BE3894F920000519 PRIMARY KEY (id,rev)
);
