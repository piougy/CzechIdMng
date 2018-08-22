--
-- CzechIdM 9.0 Flyway script
-- BCV solutions s.r.o.
--
-- This SQL script creates the required audit tables by CzechIdM (module example)

CREATE TABLE example_product_a (
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
	code varchar(255),
	code_m bit,
	description varchar(2000),
	description_m bit,
	disabled bit,
	disabled_m bit,
	name varchar(255),
	name_m bit,
	double_value numeric(38,4),
	price_m bit,
	CONSTRAINT example_product_a_pkey PRIMARY KEY (id,rev),
	CONSTRAINT fk_l8tlj6h1v48rav5nulvesvbg1 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
