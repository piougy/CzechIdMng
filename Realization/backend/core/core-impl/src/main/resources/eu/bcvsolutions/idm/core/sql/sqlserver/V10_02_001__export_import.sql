--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Export import agenda.

CREATE TABLE idm_export_import (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(6) NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	executor_name nvarchar(255) NULL,
	"name" nvarchar(255) NOT NULL,
	"type" nvarchar(45) NOT NULL,
	data_id binary(16) NULL,
	long_running_task_id binary(16) NULL,
	import_log_id binary(16) NULL,
	CONSTRAINT idm_export_import_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_export_executor ON idm_export_import(executor_name);
CREATE INDEX idx_idm_export_lrt_id ON idm_export_import(long_running_task_id);
CREATE INDEX idx_idm_export_name ON idm_export_import(name);

CREATE TABLE idm_export_import_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint NULL,
	created datetime2(6) NULL,
	created_m bit NULL,
	creator nvarchar(255) NULL,
	creator_m bit NULL,
	creator_id binary(16) NULL,
	creator_id_m bit NULL,
	modifier nvarchar(255) NULL,
	modifier_m bit NULL,
	modifier_id binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator nvarchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id binary(16) NULL,
	realm_id_m bit NULL,
	executor_name nvarchar(255) NULL,
	executor_name_m bit NULL,
	"name" nvarchar(255) NULL,
	name_m bit NULL,
	"type" nvarchar(45) NULL,
	type_m bit NULL,
	data_id binary(16) NULL,
	data_m bit NULL,
	long_running_task_id binary(16) NULL,
	long_running_task_m bit NULL,
	import_log_id binary(16) NULL,
	import_log_m bit NULL,
	CONSTRAINT idm_export_import_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fkgyab9wlkcjyc2qef37kfkkbr9 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE idm_import_log (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(6) NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	dto image NULL,
	dto_id binary(16) NULL,
	operation nvarchar(45) NOT NULL,
	parent_id binary(16) NULL,
	result_cause nvarchar(MAX) NULL,
	result_code nvarchar(255) NULL,
	result_model image NULL,
	result_state nvarchar(45) NULL,
	super_parent_id binary(16) NULL,
	"type" nvarchar(255) NOT NULL,
	batch_id binary(16) NULL,
	CONSTRAINT idm_import_log_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_i_export_log_dto_id ON idm_import_log(dto_id);
CREATE INDEX idx_i_export_log_parent_id ON idm_import_log(parent_id);
CREATE INDEX idx_i_export_log_sup_id ON idm_import_log(super_parent_id);




