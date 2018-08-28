--
-- CzechIdM 9.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This is initial SQL script for SQL server


CREATE TABLE rpt_report (
	id binary(16) NOT NULL,
	created datetime2(6) NOT NULL,
	creator NVARCHAR(255) NOT NULL,
	creator_id binary(16),
	modified datetime2(6),
	modifier NVARCHAR(255),
	modifier_id binary(16),
	original_creator NVARCHAR(255),
	original_creator_id binary(16),
	original_modifier NVARCHAR(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	executor_name NVARCHAR(255) NOT NULL,
	name NVARCHAR(255) NOT NULL,
	data_id binary(16),
	long_running_task_id binary(16),
	CONSTRAINT rpt_report_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_rpt_report_executor ON rpt_report (executor_name);
CREATE INDEX idx_rpt_report_lrt_id ON rpt_report (long_running_task_id);
CREATE INDEX idx_rpt_report_name ON rpt_report (name);

-- AUDIT TABLES

CREATE TABLE rpt_report_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime,
	created_m bit,
	creator varchar(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier varchar(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator varchar(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier varchar(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	executor_name varchar(255),
	executor_name_m bit,
	name varchar(255),
	name_m bit,
	data_id binary(16),
	data_m bit,
	long_running_task_id binary(16),
	long_running_task_m bit,
	CONSTRAINT idm_rpt_report_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_rpt_report_rev FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

