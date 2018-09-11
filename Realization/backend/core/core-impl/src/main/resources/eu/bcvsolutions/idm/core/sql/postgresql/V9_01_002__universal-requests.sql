--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- add universal request agenda

CREATE TABLE idm_request (
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
	execute_immediately bool NOT NULL,
	"name" varchar(255) NULL,
	operation varchar(255) NULL,
	owner_id bytea NULL,
	owner_type varchar(255) NOT NULL,
	request_type varchar(255) NOT NULL,
	result_cause text NULL,
	result_code varchar(255) NULL,
	result_model bytea NULL,
	result_state varchar(45) NOT NULL,
	state varchar(255) NOT NULL,
	wf_process_id varchar(255) NULL,
	CONSTRAINT idm_request_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_req_name ON idm_request USING btree (name) ;
CREATE INDEX idx_idm_req_o_id ON idm_request USING btree (owner_id) ;
CREATE INDEX idx_idm_req_o_type ON idm_request USING btree (owner_type) ;
CREATE INDEX idx_idm_req_state ON idm_request USING btree (state) ;
CREATE INDEX idx_idm_req_wf ON idm_request USING btree (wf_process_id) ;

CREATE TABLE idm_request_a (
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
	description varchar(2000) NULL,
	description_m bool NULL,
	execute_immediately bool NULL,
	execute_immediately_m bool NULL,
	"name" varchar(255) NULL,
	name_m bool NULL,
	operation varchar(255) NULL,
	operation_m bool NULL,
	request_type varchar(255) NULL,
	request_type_m bool NULL,
	state varchar(255) NULL,
	state_m bool NULL,
	wf_process_id varchar(255) NULL,
	wf_process_id_m bool NULL,
	CONSTRAINT idm_request_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_7pgo3odj3kecfk7ae7mlregjc FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE idm_request_item (
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
	"data" text NULL,
	operation varchar(255) NULL,
	owner_id bytea NULL,
	owner_type varchar(255) NOT NULL,
	result_cause text NULL,
	result_code varchar(255) NULL,
	result_model bytea NULL,
	result_state varchar(45) NOT NULL,
	state varchar(255) NOT NULL,
	super_owner_id bytea NULL,
	super_owner_type varchar(255) NULL,
	wf_process_id varchar(255) NULL,
	request_id bytea NOT NULL,
	CONSTRAINT idm_request_item_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_idm_req_item_o_id ON idm_request_item USING btree (owner_id) ;
CREATE INDEX idx_idm_req_item_o_type ON idm_request_item USING btree (owner_type) ;
CREATE INDEX idx_idm_req_item_operation ON idm_request_item USING btree (operation) ;
CREATE INDEX idx_idm_req_item_req_id ON idm_request_item USING btree (request_id) ;
CREATE INDEX idx_idm_req_item_so_id ON idm_request_item USING btree (super_owner_id) ;
CREATE INDEX idx_idm_req_item_so_type ON idm_request_item USING btree (super_owner_type) ;

CREATE TABLE idm_request_item_a (
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
	operation varchar(255) NULL,
	operation_m bool NULL,
	state varchar(255) NULL,
	state_m bool NULL,
	wf_process_id varchar(255) NULL,
	wf_process_id_m bool NULL,
	request_id bytea NULL,
	request_m bool NULL,
	CONSTRAINT idm_request_item_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_rxmtso437vofg86kgq9cu48r8 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);



