--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Provisioning queue and archive - log provisioned attributes.

CREATE TABLE sys_provisioning_attribute (
	id binary(16) NOT NULL,
	created datetime2 NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2 NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	name nvarchar(255) NOT NULL,
	provisioning_id binary(16) NOT NULL,
	removed bit NOT NULL,
	CONSTRAINT sys_provisioning_attribute_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_prov_attr_name ON sys_provisioning_attribute (name);
CREATE INDEX idx_sys_prov_attr_oper_id ON sys_provisioning_attribute (provisioning_id);