--
-- CzechIdM 10 Flyway script 
-- BCV solutions s.r.o.
--
-- Remote server agenda

CREATE TABLE sys_remote_server (
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
	host nvarchar(255) NOT NULL,
	port int NULL,
	timeout int NULL,
	use_ssl bit NOT NULL,
	description nvarchar(2000) NULL,
	CONSTRAINT sys_remote_server_pkey PRIMARY KEY (id)
);

CREATE TABLE sys_remote_server_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype numeric(19,0) NULL,
	created datetime2 NULL,
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
	host nvarchar(255) NULL,
	host_m bit NULL,
	port int NULL,
	port_m bit NULL,
	timeout int NULL,
	timeout_m bit NULL,
	use_ssl bit NULL,
	use_ssl_m bit NULL,
	description nvarchar(2000) NULL,
	description_m bit NULL,
	CONSTRAINT sys_remote_server_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fkcadsr1mgp6qgxenj0ikiqn6bg FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

ALTER TABLE sys_system ADD remote_server_id binary(16) NULL;
ALTER TABLE sys_system_a ADD remote_server_id binary(16) NULL;
ALTER TABLE sys_system_a ADD remote_server_m bit NULL;

