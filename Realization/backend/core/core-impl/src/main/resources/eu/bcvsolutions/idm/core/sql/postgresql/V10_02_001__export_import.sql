--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Export import agenda.


CREATE TABLE idm_export_import (
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
	executor_name varchar(255) NULL,
	"name" varchar(255) NOT NULL,
	"type" varchar(45) NOT NULL,
	data_id bytea NULL,
	long_running_task_id bytea NULL,
	import_log_id bytea NULL,
	CONSTRAINT idm_export_import_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_export_executor ON idm_export_import USING btree (executor_name);
CREATE INDEX idx_idm_export_lrt_id ON idm_export_import USING btree (long_running_task_id);
CREATE INDEX idx_idm_export_name ON idm_export_import USING btree (name);


CREATE TABLE idm_export_import_a (
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
	executor_name varchar(255) NULL,
	executor_name_m bool NULL,
	"name" varchar(255) NULL,
	name_m bool NULL,
	"type" varchar(45) NULL,
	type_m bool NULL,
	data_id bytea NULL,
	data_m bool NULL,
	long_running_task_id bytea NULL,
	long_running_task_m bool NULL,
	import_log_id bytea NULL,
	import_log_m bool NULL,
	CONSTRAINT idm_export_import_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fkgyab9wlkcjyc2qef37kfkkbr9 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_import_log (
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
	dto bytea NULL,
	dto_id bytea NULL,
	operation varchar(45) NOT NULL,
	parent_id bytea NULL,
	result_cause text NULL,
	result_code varchar(255) NULL,
	result_model bytea NULL,
	result_state varchar(45) NULL,
	super_parent_id bytea NULL,
	"type" varchar(255) NOT NULL,
	batch_id bytea NULL,
	CONSTRAINT idm_import_log_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_i_export_log_dto_id ON idm_import_log USING btree (dto_id);
CREATE INDEX idx_i_export_log_parent_id ON idm_import_log USING btree (parent_id);
CREATE INDEX idx_i_export_log_sup_id ON idm_import_log USING btree (super_parent_id);


