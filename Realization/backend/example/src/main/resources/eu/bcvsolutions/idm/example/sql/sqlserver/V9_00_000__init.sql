--
-- CzechIdM 9.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This is initial SQL script for SQL Server


CREATE TABLE example_product (
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
	code NVARCHAR(255) NOT NULL,
	description NVARCHAR(2000),
	disabled bit NOT NULL,
	name NVARCHAR(255) NOT NULL,
	double_value numeric(38,4),
	CONSTRAINT example_product_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_example_product_name ON example_product (name);
CREATE UNIQUE INDEX ux_example_product_code ON example_product (code);

-- AUDIT TABLES

CREATE TABLE example_product_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint,
	created datetime2(6),
	created_m bit,
	creator NVARCHAR(255),
	creator_m bit,
	creator_id binary(16),
	creator_id_m bit,
	modifier NVARCHAR(255),
	modifier_m bit,
	modifier_id binary(16),
	modifier_id_m bit,
	original_creator NVARCHAR(255),
	original_creator_m bit,
	original_creator_id binary(16),
	original_creator_id_m bit,
	original_modifier NVARCHAR(255),
	original_modifier_m bit,
	original_modifier_id binary(16),
	original_modifier_id_m bit,
	realm_id binary(16),
	realm_id_m bit,
	transaction_id binary(16),
	transaction_id_m bit,
	code NVARCHAR(255),
	code_m bit,
	description NVARCHAR(2000),
	description_m bit,
	disabled bit,
	disabled_m bit,
	name NVARCHAR(255),
	name_m bit,
	double_value numeric(38,4),
	price_m bit,
	CONSTRAINT example_product_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_l8tlj6h1v48rav5nulvesvbg1 FOREIGN KEY (rev) REFERENCES idm_audit(id)

