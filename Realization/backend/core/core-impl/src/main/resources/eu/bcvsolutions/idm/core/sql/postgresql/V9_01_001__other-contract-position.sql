--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- add other contract positions

CREATE TABLE idm_contract_position (
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
	"position" varchar(255) NULL,
	identity_contract_id bytea NOT NULL,
	work_position_id bytea NULL,
	CONSTRAINT idm_contract_position_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_contract_position_contr ON idm_contract_position USING btree (identity_contract_id) ;
CREATE INDEX idx_contract_position_pos ON idm_contract_position USING btree (work_position_id) ;
CREATE INDEX idx_idm_contract_pos_ext_id ON idm_contract_position USING btree (external_id) ;

-- audit
CREATE TABLE idm_contract_position_a (
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
	"position" varchar(255) NULL,
	position_m bool NULL,
	identity_contract_id bytea NULL,
	identity_contract_m bool NULL,
	work_position_id bytea NULL,
	work_position_m bool NULL,
	CONSTRAINT idm_contract_position_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_l51wdya2sdufbfsq1fppskrtg FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
--
-- add position relation to identity role ... wee has to know, which position assigned a role
ALTER TABLE idm_identity_role ADD COLUMN contract_position_id bytea;
CREATE INDEX idx_idm_identity_role_con_pos
  ON idm_identity_role
  USING btree
  (contract_position_id);
ALTER TABLE idm_identity_role_a ADD COLUMN contract_position_id bytea;
ALTER TABLE idm_identity_role_a ADD COLUMN contract_position_m boolean;


