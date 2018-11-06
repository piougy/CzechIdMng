--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Add entity for controlled and historic values (for provisioning merge)

CREATE TABLE sys_attribute_contr_value (
	id  binary(16) NOT NULL,
	created datetime2 NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id  binary(16) NULL,
	modified datetime2 NULL,
	modifier nvarchar(255) NULL,
	modifier_id  binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id  binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id  binary(16) NULL,
	realm_id  binary(16) NULL,
	transaction_id  binary(16) NULL,
	historic_value bit NOT NULL,
	value image NULL,
	attribute_id  binary(16) NOT NULL,
	CONSTRAINT sys_attribute_contr_value_pkey PRIMARY KEY (id)
);

CREATE TABLE sys_attribute_contr_value_a (
	id  binary(16) NOT NULL,
	rev bigint NOT NULL,
	revtype smallint NULL,
	created datetime2 NULL,
	created_m bit NULL,
	creator nvarchar(255) NULL,
	creator_m bit NULL,
	creator_id  binary(16) NULL,
	creator_id_m bit NULL,
	modifier nvarchar(255) NULL,
	modifier_m bit NULL,
	modifier_id  binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator nvarchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id  binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id  binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id  binary(16) NULL,
	realm_id_m bit NULL,
	transaction_id  binary(16) NULL,
	transaction_id_m bit NULL,
	historic_value bit NULL,
	historic_value_m bit NULL,
	value  binary(16) NULL,
	value_m bit NULL,
	attribute_id  binary(16) NULL,
	attribute_mapping_m bit NULL,
	CONSTRAINT sys_attribute_contr_value_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_fu5i4vhs025v4gpygupurphw2 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

-- Add evict_contr_values_cache column
ALTER TABLE sys_system_attribute_mapping ADD COLUMN evict_contr_values_cache boolean;
GO
ALTER TABLE sys_system_attribute_mapping_a ADD COLUMN evict_contr_values_cache boolean;
ALTER TABLE sys_system_attribute_mapping_a ADD COLUMN evict_controlled_values_cache_m boolean;
-- Set evict_contr_values_cache to true for all attributes (I cannot update only MERGE attributes, because some attributes could have set different strategy )
update	sys_system_attribute_mapping set evict_contr_values_cache = true;
ALTER TABLE sys_system_attribute_mapping ALTER COLUMN evict_contr_values_cache SET NOT NULL;