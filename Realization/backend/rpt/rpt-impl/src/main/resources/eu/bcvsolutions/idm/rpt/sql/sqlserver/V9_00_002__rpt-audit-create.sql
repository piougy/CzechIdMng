--
-- CzechIdM 9.0 Flyway script
-- BCV solutions s.r.o.
--
-- This SQL script creates the required audit tables by CzechIdM (module rpt)

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
