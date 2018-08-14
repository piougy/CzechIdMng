--
-- CzechIdM 9.0 Flyway script 
-- BCV solutions s.r.o.
--
-- This SQL script creates the required tables by CzechIdM (module example)

CREATE TABLE example_product (
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
	code varchar(255) NOT NULL,
	description varchar(2000),
	disabled bit NOT NULL,
	name varchar(255) NOT NULL,
	double_value numeric(38,4),
	CONSTRAINT example_product_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_example_product_name ON example_product (name);
CREATE UNIQUE INDEX ux_example_product_code ON example_product (code);
