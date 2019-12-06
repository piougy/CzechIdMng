--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add other contract positions

CREATE TABLE idm_contract_position (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier nvarchar(255),
	modifier_id binary(16),
	original_creator nvarchar(255),
	original_creator_id binary(16),
	original_modifier nvarchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	external_id nvarchar(255),	
	"position" nvarchar(255),
	identity_contract_id binary(16) NOT NULL,
	work_position_id binary(16) NULL,
	CONSTRAINT idm_contract_position_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_contract_position_contr ON idm_contract_position (identity_contract_id) ;
CREATE INDEX idx_contract_position_pos ON idm_contract_position (work_position_id) ;
CREATE INDEX idx_idm_contract_pos_ext_id ON idm_contract_position (external_id) ;

-- audit
CREATE TABLE idm_contract_position_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator nvarchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier nvarchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator nvarchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier nvarchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	external_id nvarchar(255),
	external_id_m bit,
	"position" nvarchar(255),
	position_m bit,
	identity_contract_id binary(16),
	identity_contract_m bit,
	work_position_id binary(16),
	work_position_m bit,
	CONSTRAINT idm_contract_position_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_l51wdya2sdufbfsq1fppskrtg FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
--
-- add position relation to identity role ... wee has to know, which position assigned a role
ALTER TABLE idm_identity_role ADD contract_position_id binary(16);
CREATE INDEX idx_idm_identity_role_con_pos
  ON idm_identity_role
  (contract_position_id);
ALTER TABLE idm_identity_role_a ADD contract_position_id binary(16);
ALTER TABLE idm_identity_role_a ADD contract_position_m bit;


