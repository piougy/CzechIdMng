--
-- CzechIdM 9.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM (module rpt)


CREATE TABLE rpt_report (
	id binary(16) NOT NULL,
	created datetime NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id binary(16),
	modified datetime,
	modifier varchar(255),
	modifier_id binary(16),
	original_creator varchar(255),
	original_creator_id binary(16),
	original_modifier varchar(255),
	original_modifier_id binary(16),
	realm_id binary(16),
	transaction_id binary(16),
	executor_name varchar(255) NOT NULL,
	name varchar(255) NOT NULL,
	data_id binary(16),
	long_running_task_id binary(16),
	CONSTRAINT rpt_report_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_rpt_report_executor ON rpt_report (executor_name);
CREATE INDEX idx_rpt_report_lrt_id ON rpt_report (long_running_task_id);
CREATE INDEX idx_rpt_report_name ON rpt_report (name);
