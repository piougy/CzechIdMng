--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Provisioning queue and archive - log provisioned attributes.

CREATE TABLE sys_provisioning_attribute (
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
	name varchar(255) NOT NULL,
	provisioning_id bytea NOT NULL,
	removed bool NOT NULL,
	CONSTRAINT sys_provisioning_attribute_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_sys_prov_attr_name ON sys_provisioning_attribute USING btree (name);
CREATE INDEX idx_sys_prov_attr_oper_id ON sys_provisioning_attribute USING btree (provisioning_id);